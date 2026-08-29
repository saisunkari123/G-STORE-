import express from "express";
import cors from "cors";
import dotenv from "dotenv";
import crypto from "crypto";
import { executeGraphQL } from "./appsync.js";

dotenv.config();

const app = express();

// Full CORS & header exposure
app.use(
  cors({
    origin: "*",
    methods: ["GET", "POST", "OPTIONS", "HEAD"],
    allowedHeaders: ["*"],
    exposedHeaders: ["*"],
  })
);

app.use(express.json({ limit: "50mb" }));

const PORT = process.env.PORT || 3000;

// Cloudinary configuration
const CLOUDINARY_CLOUD_NAME = process.env.CLOUDINARY_CLOUD_NAME || "k1lw675z";
const CLOUDINARY_API_KEY = process.env.CLOUDINARY_API_KEY || "498889461713286";
const CLOUDINARY_API_SECRET = process.env.CLOUDINARY_API_SECRET || "8SR-robZhuJf-5ehvJTFCscCatY";

async function uploadToCloudinary(imageUrlOrData: string): Promise<string> {
  try {
    if (imageUrlOrData.includes(`res.cloudinary.com/${CLOUDINARY_CLOUD_NAME}`)) {
      return imageUrlOrData;
    }

    const timestamp = Math.floor(Date.now() / 1000).toString();
    const folder = "ricemart_products";
    const signStr = `folder=${folder}&timestamp=${timestamp}${CLOUDINARY_API_SECRET}`;
    const signature = crypto.createHash("sha1").update(signStr).digest("hex");

    const formData = new URLSearchParams();
    formData.append("file", imageUrlOrData);
    formData.append("api_key", CLOUDINARY_API_KEY);
    formData.append("timestamp", timestamp);
    formData.append("folder", folder);
    formData.append("signature", signature);

    const res = await fetch(`https://api.cloudinary.com/v1_1/${CLOUDINARY_CLOUD_NAME}/image/upload`, {
      method: "POST",
      body: formData,
    });

    const data: any = await res.json();
    if (data && data.secure_url) {
      return data.secure_url;
    }
    return imageUrlOrData;
  } catch (err: any) {
    console.warn("Cloudinary upload fallback to direct URL:", err.message);
    return imageUrlOrData;
  }
}

// High-quality category placeholder images
const categoryPlaceholders: Record<string, string> = {
  c_rice: "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600&auto=format&fit=crop",
  c_oil: "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=600&auto=format&fit=crop",
  c_dal: "https://images.unsplash.com/photo-1585994192701-f1a505c8574a?w=600&auto=format&fit=crop",
  c_dairy: "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600&auto=format&fit=crop",
  c_spices: "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=600&auto=format&fit=crop",
};

// Category mapping helper
const categoryNames: Record<string, string> = {
  c_rice: "Rice Bags",
  c_oil: "Cooking Oils",
  c_dal: "Dals & Pulses",
  c_dairy: "Dairy Essentials",
  c_spices: "Spices & Masalas",
};

function normalizeCategory(cat: string): string {
  const lower = (cat || "").toLowerCase().trim();
  if (lower.includes("rice") || lower === "c_rice") return "c_rice";
  if (lower.includes("oil") || lower === "c_oil") return "c_oil";
  if (lower.includes("dal") || lower.includes("pulse") || lower === "c_dal") return "c_dal";
  if (lower.includes("dairy") || lower.includes("milk") || lower.includes("curd") || lower.includes("ghee") || lower.includes("paneer") || lower === "c_dairy") return "c_dairy";
  if (lower.includes("spice") || lower.includes("masala") || lower === "c_spices") return "c_spices";
  return "c_rice"; // default
}

// Master Tools List conforming to MCP Specification (2024-11-05)
const toolsList = [
  {
    name: "list_products",
    description:
      "Retrieve all products available in G-Store, including category, description, and variants (sizes like 5kg, 10kg, 26kg, selling price, MRP, and stock status).",
    inputSchema: {
      type: "object",
      properties: {
        category: {
          type: "string",
          description: "Optional category filter (e.g., 'Rice Bags', 'Cooking Oils', 'Dals & Pulses', 'Dairy Essentials', 'Spices & Masalas')",
        },
        includeUnlisted: {
          type: "boolean",
          description: "If true, includes hidden/unlisted products as well. Default false.",
        },
      },
    },
  },
  {
    name: "get_product_by_id",
    description: "Get detailed information for a specific product by its ID.",
    inputSchema: {
      type: "object",
      properties: {
        productId: {
          type: "string",
          description: "The unique ID of the product (e.g., 'p_rice_sona', 'p_rice_basmati')",
        },
      },
      required: ["productId"],
    },
  },
  {
    name: "create_product",
    description:
      "Create and add a new product to G-Store catalog with pricing, weight size, stock, and image. Uploads the image directly to store's Cloudinary storage and saves to AWS AppSync.",
    inputSchema: {
      type: "object",
      properties: {
        name: {
          type: "string",
          description: "Product display name (e.g., 'Amul Taaza Homogenised Toned Milk', 'Freedom Refined Sunflower Oil')",
        },
        category: {
          type: "string",
          description: "Product category: 'Dairy Essentials', 'Rice Bags', 'Cooking Oils', 'Dals & Pulses', or 'Spices & Masalas'",
        },
        price: {
          type: "number",
          description: "Selling price in ₹ (e.g., 34 for 500ml milk packet, 1350 for 26kg rice)",
        },
        mrp: {
          type: "number",
          description: "Optional printed MRP in ₹ (e.g., 38)",
        },
        size: {
          type: "string",
          description: "Weight or volume size (e.g., '500ml', '1L', '26kg', '10kg', '1kg').",
        },
        stock: {
          type: "number",
          description: "Available stock quantity in units/packets (default 50)",
        },
        description: {
          type: "string",
          description: "Optional product description or brand notes",
        },
        imageUrl: {
          type: "string",
          description: "Optional image URL. Automatically uploaded to Cloudinary folder 'ricemart_products'. If omitted, a clean category placeholder image is assigned.",
        },
      },
      required: ["name", "category", "price"],
    },
  },
  {
    name: "delete_product",
    description: "Delete or remove a product from the G-Store catalog by its ID.",
    inputSchema: {
      type: "object",
      properties: {
        productId: {
          type: "string",
          description: "The ID of the product to delete (e.g., 'p_rice_sona')",
        },
      },
      required: ["productId"],
    },
  },
  {
    name: "update_product_price_or_stock",
    description:
      "Update the price or stock quantity of a specific product variant (e.g., set 26kg Sona Masuri price to ₹1400 or stock to 40).",
    inputSchema: {
      type: "object",
      properties: {
        productId: {
          type: "string",
          description: "The product ID (e.g. 'p_rice_sona')",
        },
        variantSize: {
          type: "string",
          description: "The variant weight/size (e.g., '26kg', '10kg', '5kg')",
        },
        newPrice: {
          type: "number",
          description: "Optional new selling price in ₹",
        },
        stock: {
          type: "number",
          description: "Optional new stock bag quantity",
        },
      },
      required: ["productId"],
    },
  },
  {
    name: "get_low_stock_alerts",
    description: "Identify and list all products running low on stock (stock count below threshold, e.g. < 10 units/bags) so the store owner knows what to reorder.",
    inputSchema: {
      type: "object",
      properties: {
        threshold: {
          type: "number",
          description: "Stock threshold to trigger low stock alert (default is 10 units/bags)",
        },
      },
    },
  },
  {
    name: "list_orders",
    description:
      "Fetch recent customer orders from G-Store. Allows filtering by status (PENDING, PREPARING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED).",
    inputSchema: {
      type: "object",
      properties: {
        status: {
          type: "string",
          description:
            "Optional order status filter: PENDING, PREPARING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED",
        },
        limit: {
          type: "number",
          description: "Maximum number of orders to return (default 20)",
        },
      },
    },
  },
  {
    name: "search_customer_orders",
    description: "Search customer orders, delivery history, and lifetime spending by customer phone number or customer name.",
    inputSchema: {
      type: "object",
      properties: {
        query: {
          type: "string",
          description: "Customer mobile number (e.g., '9704173515') or customer name (e.g., 'sunny')",
        },
      },
      required: ["query"],
    },
  },
  {
    name: "update_order_status",
    description:
      "Update the fulfillment status of an order (e.g. advance from PENDING to PREPARING, OUT_FOR_DELIVERY, or DELIVERED).",
    inputSchema: {
      type: "object",
      properties: {
        orderId: {
          type: "string",
          description: "The ID of the order to update (e.g. 'G-1786896453001-823')",
        },
        newStatus: {
          type: "string",
          enum: ["PENDING", "PREPARING", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED"],
          description: "The new order status",
        },
      },
      required: ["orderId", "newStatus"],
    },
  },
  {
    name: "get_best_selling_products",
    description: "Calculate the top best-selling products in G-Store ranked by total bags/units sold and revenue generated from completed orders.",
    inputSchema: {
      type: "object",
      properties: {
        limit: {
          type: "number",
          description: "Number of top products to return (default is 5)",
        },
      },
    },
  },
  {
    name: "get_store_metrics",
    description:
      "Get summary store analytics: total orders, total revenue, pending orders, and recent order activity.",
    inputSchema: {
      type: "object",
      properties: {},
    },
  },
  {
    name: "get_store_settings",
    description:
      "Get the active store settings such as delivery radius (in km) and minimum order value (in ₹).",
    inputSchema: {
      type: "object",
      properties: {},
    },
  },
  {
    name: "update_store_settings",
    description:
      "Update store operational settings such as delivery radius (km) or minimum order amount (₹).",
    inputSchema: {
      type: "object",
      properties: {
        minimumOrderAmount: {
          type: "number",
          description: "New minimum order amount in ₹",
        },
        deliveryRadiusKm: {
          type: "number",
          description: "New delivery radius in kilometers",
        },
      },
    },
  },
];

// Helper to format variants from raw AppSync representation
function formatVariants(rawVariants: any[] = []) {
  return rawVariants.map((v) => {
    const rawSize = v.size || "";
    const parts = rawSize.split(":::");
    if (parts.length >= 2) {
      const weight = `${parts[0]} ${parts[1]}`;
      const mrp = parts[2] ? Number(parts[2]) : v.price;
      const sku = parts[3] || "";
      const variantId = parts[4] || "";
      return {
        variantId,
        weight,
        sellingPrice: `₹${v.price}`,
        mrp: `₹${mrp}`,
        priceNumber: v.price,
        mrpNumber: mrp,
        stock: v.stock,
        inStock: v.stock > 0,
        sku,
        rawSize,
      };
    }
    return {
      size: rawSize,
      price: `₹${v.price}`,
      priceNumber: v.price,
      stock: v.stock,
      inStock: v.stock > 0,
      rawSize,
    };
  });
}

// Helper to format orders
function formatOrder(o: any) {
  const addressParts = (o.deliveryAddress || "").split(":::");
  const addressType = addressParts[0]?.trim() || "Home";
  const addressText = addressParts[1]?.trim() || o.deliveryAddress;
  const customerPhone = addressParts[2]?.trim() || "";
  const distanceKm = addressParts[3] ? parseFloat(addressParts[3]).toFixed(1) + " km" : undefined;

  return {
    orderId: o.id,
    customerId: o.customerId,
    customerName: o.customerName,
    customerPhone,
    status: o.status,
    total: `₹${o.total}`,
    totalAmount: o.total,
    subtotal: o.subtotal ? `₹${o.subtotal}` : undefined,
    deliveryFee: o.deliveryFee ? `₹${o.deliveryFee}` : undefined,
    deliveryAddress: addressText,
    addressType,
    distanceKm,
    createdAt: o.createdAt,
    items: (o.items || []).map((item: any) => ({
      productId: item.productId,
      productName: item.productName,
      quantity: item.quantity,
      size: item.variantSize,
      price: `₹${item.price}`,
    })),
  };
}

// Tool execution logic querying live AWS AppSync backend
async function executeToolCall(name: string, args: any) {
  switch (name) {
    case "list_products": {
      const query = `
        query ListProducts {
          listProducts(limit: 1000) {
            items {
              id
              name
              category
              description
              imageUrls
              variants {
                size
                price
                stock
              }
            }
          }
        }
      `;
      const data = await executeGraphQL(query);
      let rawItems = data?.listProducts?.items || [];

      // Filter out metadata records
      rawItems = rawItems.filter((p: any) => !p.id.startsWith("sys_") && p.category !== "metadata");

      const products = rawItems
        .map((p: any) => {
          const descParts = (p.description || "").split(" ::: ");
          const isListed = descParts.length > 6 ? descParts[6] !== "false" : true;
          return {
            id: p.id,
            name: p.name,
            category: categoryNames[p.category] || p.category,
            description: p.description,
            isListed,
            imageUrls: p.imageUrls || [],
            variants: formatVariants(p.variants),
          };
        })
        .filter((p: any) => args?.includeUnlisted || p.isListed);

      if (args?.category) {
        const filter = String(args.category).toLowerCase();
        const filtered = products.filter(
          (p: any) =>
            p.category.toLowerCase().includes(filter) ||
            p.name.toLowerCase().includes(filter)
        );
        return { count: filtered.length, products: filtered };
      }

      return { count: products.length, products };
    }

    case "get_product_by_id": {
      const query = `
        query GetProduct($id: ID!) {
          getProduct(id: $id) {
            id
            name
            category
            description
            imageUrls
            variants {
              size
              price
              stock
            }
          }
        }
      `;
      const data = await executeGraphQL(query, { id: args?.productId });
      const p = data?.getProduct;
      if (!p) return { message: "Product not found" };

      const descParts = (p.description || "").split(" ::: ");
      const isListed = descParts.length > 6 ? descParts[6] !== "false" : true;

      return {
        id: p.id,
        name: p.name,
        category: categoryNames[p.category] || p.category,
        description: p.description,
        isListed,
        imageUrls: p.imageUrls || [],
        variants: formatVariants(p.variants),
      };
    }

    case "create_product": {
      const categoryKey = normalizeCategory(args?.category);
      const cleanName = String(args.name).trim();
      const slug = cleanName.toLowerCase().replace(/[^a-z0-9]+/g, "_").slice(0, 20);
      const productId = `p_${categoryKey.replace("c_", "")}_${slug}_${Date.now().toString().slice(-4)}`;

      const sellingPrice = Number(args.price);
      const mrpPrice = args.mrp ? Number(args.mrp) : Math.round(sellingPrice * 1.15);
      const stockCount = args.stock !== undefined ? Number(args.stock) : 50;

      // Parse size input into value and unit
      const rawSizeInput = (args.size || (categoryKey === "c_rice" ? "26kg" : categoryKey === "c_oil" ? "1L" : categoryKey === "c_dairy" ? "500ml" : "1kg")).trim();
      const sizeMatch = rawSizeInput.match(/^(\d+(?:\.\d+)?)\s*([a-zA-Z]+)$/);
      const unitVal = sizeMatch ? sizeMatch[1] : (categoryKey === "c_rice" ? "26" : "500");
      const unitType = sizeMatch ? sizeMatch[2] : (categoryKey === "c_rice" ? "kg" : "ml");

      const variantId = `v_${productId}_${unitVal}${unitType}`;
      const sku = `SKU-${categoryKey.toUpperCase().replace("C_", "")}-${slug.toUpperCase().slice(0, 6)}-${unitVal}`;
      const variantSizeString = `${unitVal}:::${unitType}:::${mrpPrice.toFixed(1)}:::${sku}:::${variantId}`;

      // Upload image to Cloudinary
      const sourceImage = args.imageUrl || categoryPlaceholders[categoryKey] || categoryPlaceholders["c_dairy"];
      const finalCloudinaryUrl = await uploadToCloudinary(sourceImage);

      const createMutation = `
        mutation CreateProduct($input: CreateProductInput!) {
          createProduct(input: $input) {
            id
            name
            category
            description
            imageUrls
            variants {
              size
              price
              stock
            }
          }
        }
      `;

      const input = {
        id: productId,
        name: cleanName,
        category: categoryKey,
        description: `G-Store :::  ::: ${cleanName} :::  ::: ${args.description || cleanName} :::  ::: true`,
        imageUrls: [finalCloudinaryUrl],
        variants: [
          {
            size: variantSizeString,
            price: sellingPrice,
            stock: stockCount,
          },
        ],
      };

      const result = await executeGraphQL(createMutation, { input });
      const created = result?.createProduct;

      return {
        message: "Product created successfully in G-Store catalog & uploaded to Cloudinary",
        product: {
          id: created?.id,
          name: created?.name,
          category: categoryNames[created?.category] || created?.category,
          description: created?.description,
          isListed: true,
          cloudinaryImageUrl: finalCloudinaryUrl,
          variants: formatVariants(created?.variants),
        },
      };
    }

    case "delete_product": {
      const mutation = `
        mutation DeleteProduct($input: DeleteProductInput!) {
          deleteProduct(input: $input) {
            id
            name
          }
        }
      `;
      const result = await executeGraphQL(mutation, {
        input: { id: args?.productId },
      });

      return {
        message: `Product ${args?.productId} deleted successfully from G-Store catalog`,
        deletedProduct: result?.deleteProduct,
      };
    }

    case "update_product_price_or_stock": {
      const getQuery = `
        query GetProduct($id: ID!) {
          getProduct(id: $id) {
            id
            name
            category
            description
            imageUrls
            variants {
              size
              price
              stock
            }
          }
        }
      `;
      const existingData = await executeGraphQL(getQuery, { id: args?.productId });
      const product = existingData?.getProduct;

      if (!product) {
        throw new Error(`Product with ID ${args?.productId} not found.`);
      }

      let updatedVariants = (product.variants || []).map((v: any) => {
        const isMatch =
          !args?.variantSize ||
          v.size.toLowerCase().includes(String(args.variantSize).toLowerCase());

        if (isMatch) {
          return {
            ...v,
            price: args?.newPrice !== undefined ? args.newPrice : v.price,
            stock: args?.stock !== undefined ? args.stock : v.stock,
          };
        }
        return v;
      });

      const updateMutation = `
        mutation UpdateProduct($input: UpdateProductInput!) {
          updateProduct(input: $input) {
            id
            name
            variants {
              size
              price
              stock
            }
          }
        }
      `;

      const updateResult = await executeGraphQL(updateMutation, {
        input: {
          id: product.id,
          variants: updatedVariants,
        },
      });

      return {
        message: "Product updated successfully",
        product: {
          id: updateResult?.updateProduct?.id,
          name: updateResult?.updateProduct?.name,
          variants: formatVariants(updateResult?.updateProduct?.variants),
        },
      };
    }

    case "get_low_stock_alerts": {
      const threshold = typeof args?.threshold === "number" ? args.threshold : 10;
      const query = `
        query ListProducts {
          listProducts(limit: 1000) {
            items {
              id
              name
              category
              variants {
                size
                price
                stock
              }
            }
          }
        }
      `;
      const data = await executeGraphQL(query);
      const rawItems = (data?.listProducts?.items || []).filter(
        (p: any) => !p.id.startsWith("sys_") && p.category !== "metadata"
      );

      const lowStockItems: any[] = [];

      for (const prod of rawItems) {
        const formatted = formatVariants(prod.variants);
        for (const variant of formatted) {
          if (variant.stock <= threshold) {
            lowStockItems.push({
              productId: prod.id,
              productName: prod.name,
              category: categoryNames[prod.category] || prod.category,
              weight: variant.weight || variant.size,
              price: variant.sellingPrice || variant.price,
              currentStock: variant.stock,
              alertStatus: variant.stock === 0 ? "🚨 OUT OF STOCK" : "⚠️ LOW STOCK",
            });
          }
        }
      }

      lowStockItems.sort((a, b) => a.currentStock - b.currentStock);

      return {
        alertCount: lowStockItems.length,
        thresholdUsed: threshold,
        summary:
          lowStockItems.length === 0
            ? "All products have sufficient stock levels!"
            : `Found ${lowStockItems.length} product variants with stock <= ${threshold} units`,
        lowStockItems,
      };
    }

    case "list_orders": {
      const limit = typeof args?.limit === "number" ? args.limit : 50;
      const query = `
        query ListOrders($limit: Int) {
          listOrders(limit: $limit) {
            items {
              id
              customerId
              customerName
              deliveryAddress
              deliveryFee
              latitude
              longitude
              status
              subtotal
              total
              createdAt
              items {
                productId
                productName
                quantity
                price
                variantSize
              }
            }
          }
        }
      `;
      const data = await executeGraphQL(query, { limit });
      let orders = data?.listOrders?.items || [];

      if (args?.status) {
        const targetStatus = String(args.status).toUpperCase();
        orders = orders.filter(
          (o: any) => o.status && o.status.toUpperCase() === targetStatus
        );
      }

      const formattedOrders = orders.map(formatOrder);
      return { count: formattedOrders.length, orders: formattedOrders };
    }

    case "search_customer_orders": {
      const queryStr = String(args?.query || "").toLowerCase().trim();
      const query = `
        query ListOrders {
          listOrders(limit: 1000) {
            items {
              id
              customerId
              customerName
              deliveryAddress
              deliveryFee
              status
              subtotal
              total
              createdAt
              items {
                productId
                productName
                quantity
                price
                variantSize
              }
            }
          }
        }
      `;
      const data = await executeGraphQL(query);
      const allOrders = (data?.listOrders?.items || []).map(formatOrder);

      const matched = allOrders.filter(
        (o: any) =>
          o.customerName?.toLowerCase().includes(queryStr) ||
          o.customerPhone?.includes(queryStr) ||
          o.customerId?.toLowerCase().includes(queryStr) ||
          o.orderId?.toLowerCase().includes(queryStr)
      );

      const totalSpend = matched.reduce((sum: number, o: any) => sum + (o.totalAmount || 0), 0);

      return {
        query: args?.query,
        matchedOrdersCount: matched.length,
        customerLifetimeSpend: `₹${totalSpend.toFixed(2)}`,
        customerName: matched[0]?.customerName || undefined,
        customerPhone: matched[0]?.customerPhone || undefined,
        orders: matched,
      };
    }

    case "update_order_status": {
      const mutation = `
        mutation UpdateOrder($input: UpdateOrderInput!) {
          updateOrder(input: $input) {
            id
            status
            updatedAt
          }
        }
      `;
      const result = await executeGraphQL(mutation, {
        input: {
          id: args?.orderId,
          status: args?.newStatus,
        },
      });

      return { message: "Order status updated successfully", order: result?.updateOrder };
    }

    case "get_best_selling_products": {
      const limit = typeof args?.limit === "number" ? args.limit : 5;
      const query = `
        query ListOrders {
          listOrders(limit: 1000) {
            items {
              id
              status
              items {
                productId
                productName
                quantity
                price
                variantSize
              }
            }
          }
        }
      `;
      const data = await executeGraphQL(query);
      const orders = (data?.listOrders?.items || []).filter((o: any) => o.status !== "CANCELLED");

      const productSalesMap: Record<
        string,
        { productId: string; productName: string; variantSize: string; totalQuantitySold: number; totalRevenue: number; ordersCount: number }
      > = {};

      for (const order of orders) {
        for (const item of order.items || []) {
          const key = `${item.productId}_${item.variantSize || "std"}`;
          if (!productSalesMap[key]) {
            productSalesMap[key] = {
              productId: item.productId,
              productName: item.productName,
              variantSize: item.variantSize || "Standard",
              totalQuantitySold: 0,
              totalRevenue: 0,
              ordersCount: 0,
            };
          }
          productSalesMap[key].totalQuantitySold += Number(item.quantity) || 1;
          productSalesMap[key].totalRevenue += (Number(item.price) || 0) * (Number(item.quantity) || 1);
          productSalesMap[key].ordersCount += 1;
        }
      }

      const leaderboard = Object.values(productSalesMap)
        .sort((a, b) => b.totalQuantitySold - a.totalQuantitySold)
        .slice(0, limit)
        .map((p, idx) => ({
          rank: idx + 1,
          productName: p.productName,
          size: p.variantSize,
          totalQuantitySold: `${p.totalQuantitySold} units/bags`,
          totalRevenueGenerated: `₹${p.totalRevenue.toFixed(2)}`,
          orderAppearances: p.ordersCount,
        }));

      return {
        topBestSellersCount: leaderboard.length,
        leaderboard,
      };
    }

    case "get_store_metrics": {
      const ordersQuery = `
        query ListOrders {
          listOrders(limit: 1000) {
            items {
              id
              status
              total
              createdAt
            }
          }
        }
      `;
      const data = await executeGraphQL(ordersQuery);
      const orders = data?.listOrders?.items || [];

      const totalOrders = orders.length;
      const pendingOrders = orders.filter((o: any) => o.status === "PENDING").length;
      const preparingOrders = orders.filter((o: any) => o.status === "PREPARING").length;
      const outForDelivery = orders.filter((o: any) => o.status === "OUT_FOR_DELIVERY").length;
      const deliveredOrders = orders.filter((o: any) => o.status === "DELIVERED").length;
      const cancelledOrders = orders.filter((o: any) => o.status === "CANCELLED").length;

      const totalRevenue = orders
        .filter((o: any) => o.status !== "CANCELLED")
        .reduce((sum: number, o: any) => sum + (Number(o.total) || 0), 0);

      return {
        metrics: {
          totalOrders,
          totalRevenue: `₹${totalRevenue.toFixed(2)}`,
          totalRevenueNumber: totalRevenue,
          statusBreakdown: {
            pending: pendingOrders,
            preparing: preparingOrders,
            outForDelivery: outForDelivery,
            delivered: deliveredOrders,
            cancelled: cancelledOrders,
          },
        },
      };
    }

    case "get_store_settings": {
      const query = `
        query GetAppConfig {
          getProduct(id: "sys_config") {
            id
            name
            description
          }
        }
      `;
      const data = await executeGraphQL(query);
      const sysProd = data?.getProduct;

      let config = { minimumOrderAmount: 150.0, deliveryRadiusKm: 10.0 };
      if (sysProd && sysProd.description) {
        try {
          config = JSON.parse(sysProd.description);
        } catch {
          // fallback
        }
      }

      return {
        minimumOrderAmount: config.minimumOrderAmount ?? 150.0,
        deliveryRadiusKm: config.deliveryRadiusKm ?? 10.0,
        formatted: {
          minimumOrder: `₹${config.minimumOrderAmount ?? 150.0}`,
          deliveryRadius: `${config.deliveryRadiusKm ?? 10.0} km`,
        },
      };
    }

    case "update_store_settings": {
      const getQuery = `
        query GetAppConfig {
          getProduct(id: "sys_config") {
            id
            description
          }
        }
      `;
      const existingData = await executeGraphQL(getQuery);
      const existing = existingData?.getProduct;

      let currentConfig = { minimumOrderAmount: 150.0, deliveryRadiusKm: 10.0 };
      if (existing && existing.description) {
        try {
          currentConfig = JSON.parse(existing.description);
        } catch {
          // fallback
        }
      }

      const updatedConfig = {
        minimumOrderAmount:
          args?.minimumOrderAmount !== undefined ? args.minimumOrderAmount : currentConfig.minimumOrderAmount,
        deliveryRadiusKm:
          args?.deliveryRadiusKm !== undefined ? args.deliveryRadiusKm : currentConfig.deliveryRadiusKm,
      };

      if (existing) {
        const updateMutation = `
          mutation UpdateConfig($input: UpdateProductInput!) {
            updateProduct(input: $input) {
              id
              description
            }
          }
        `;
        await executeGraphQL(updateMutation, {
          input: {
            id: "sys_config",
            description: JSON.stringify(updatedConfig),
          },
        });
      } else {
        const createMutation = `
          mutation CreateConfig($input: CreateProductInput!) {
            createProduct(input: $input) {
              id
              description
            }
          }
        `;
        await executeGraphQL(createMutation, {
          input: {
            id: "sys_config",
            name: "System App Config",
            category: "metadata",
            description: JSON.stringify(updatedConfig),
            variants: [],
          },
        });
      }

      return {
        message: "Store settings updated successfully",
        settings: {
          minimumOrderAmount: updatedConfig.minimumOrderAmount,
          deliveryRadiusKm: updatedConfig.deliveryRadiusKm,
          formatted: {
            minimumOrder: `₹${updatedConfig.minimumOrderAmount}`,
            deliveryRadius: `${updatedConfig.deliveryRadiusKm} km`,
          },
        },
      };
    }

    default:
      throw new Error(`Unknown tool name: ${name}`);
  }
}

// Master JSON-RPC MCP Dispatcher
async function handleJsonRpc(body: any, res: express.Response) {
  if (!body || typeof body !== "object") {
    return res.status(400).json({ jsonrpc: "2.0", error: { code: -32700, message: "Parse error" }, id: null });
  }

  const { id, method, params } = body;

  switch (method) {
    case "initialize":
      return res.json({
        jsonrpc: "2.0",
        id: id ?? 1,
        result: {
          protocolVersion: params?.protocolVersion || "2024-11-05",
          capabilities: {
            tools: {
              listChanged: false,
            },
          },
          serverInfo: {
            name: "gstore-mcp-server",
            version: "1.2.0",
          },
        },
      });

    case "notifications/initialized":
      return res.status(200).end();

    case "ping":
      return res.json({
        jsonrpc: "2.0",
        id: id ?? 1,
        result: {},
      });

    case "tools/list":
      return res.json({
        jsonrpc: "2.0",
        id: id ?? 1,
        result: {
          tools: toolsList,
        },
      });

    case "tools/call": {
      try {
        const toolResult = await executeToolCall(params?.name, params?.arguments);
        return res.json({
          jsonrpc: "2.0",
          id: id ?? 1,
          result: {
            content: [
              {
                type: "text",
                text: JSON.stringify(toolResult, null, 2),
              },
            ],
          },
        });
      } catch (err: any) {
        return res.json({
          jsonrpc: "2.0",
          id: id ?? 1,
          result: {
            content: [
              {
                type: "text",
                text: `Error: ${err.message}`,
              },
            ],
            isError: true,
          },
        });
      }
    }

    default:
      return res.json({
        jsonrpc: "2.0",
        id: id ?? null,
        error: {
          code: -32601,
          message: `Method not found: ${method}`,
        },
      });
  }
}

// SSE Connection Handler
function handleSse(req: express.Request, res: express.Response) {
  res.setHeader("Content-Type", "text/event-stream");
  res.setHeader("Cache-Control", "no-cache, no-transform");
  res.setHeader("Connection", "keep-alive");
  res.setHeader("Access-Control-Allow-Origin", "*");

  const sessionId = Math.random().toString(36).substring(2);
  res.write(`event: endpoint\ndata: /mcp?sessionId=${sessionId}\n\n`);

  const keepAlive = setInterval(() => {
    res.write(": keepalive\n\n");
  }, 15000);

  req.on("close", () => {
    clearInterval(keepAlive);
  });
}

// Unified route handlers for /mcp, /sse, and root
app.get("/sse", handleSse);
app.get("/mcp", (req, res) => {
  const accept = req.headers.accept || "";
  if (accept.includes("text/event-stream")) {
    return handleSse(req, res);
  }
  res.json({
    status: "online",
    name: "G-Store MCP Server",
    version: "1.2.0",
    protocolVersion: "2024-11-05",
    toolsCount: toolsList.length,
    tools: toolsList.map((t) => t.name),
  });
});

app.post("/mcp", (req, res) => handleJsonRpc(req.body, res));
app.post("/sse", (req, res) => handleJsonRpc(req.body, res));
app.post("/message", (req, res) => handleJsonRpc(req.body, res));

// Health check endpoint
app.get("/", (req, res) => {
  res.json({
    status: "online",
    name: "G-Store MCP Server",
    version: "1.2.0",
    protocolVersion: "2024-11-05",
    toolsCount: toolsList.length,
    endpoints: {
      mcp: "/mcp",
      sse: "/sse",
    },
  });
});

app.post("/", (req, res) => handleJsonRpc(req.body, res));

app.listen(PORT, () => {
  console.log(`🚀 G-Store MCP Server v1.2.0 listening on http://localhost:${PORT}`);
  console.log(`📡 Cloudinary & AppSync bridge active for all ${toolsList.length} tools`);
});
