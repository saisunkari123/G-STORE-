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

function formatCloudinaryUrl(url: string): string {
  if (url && url.includes("res.cloudinary.com") && url.includes("/upload/")) {
    if (!url.includes("/upload/c_") && !url.includes("/upload/w_")) {
      return url.replace("/upload/", "/upload/c_fill,g_auto,w_800,h_800,f_auto,q_auto/");
    }
  }
  return url;
}

async function uploadToCloudinary(imageUrlOrData: string): Promise<string> {
  try {
    if (imageUrlOrData.includes(`res.cloudinary.com/${CLOUDINARY_CLOUD_NAME}`)) {
      return formatCloudinaryUrl(imageUrlOrData);
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
      return formatCloudinaryUrl(data.secure_url);
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
  c_snacks: "https://images.unsplash.com/photo-1621996346565-e3d5d6281290?w=600&auto=format&fit=crop",
};

// Category mapping helper
const categoryNames: Record<string, string> = {
  c_rice: "Rice Bags",
  c_oil: "Cooking Oils",
  c_dal: "Dals & Pulses",
  c_dairy: "Dairy Essentials",
  c_spices: "Spices & Masalas",
  c_snacks: "Snacks & Beverages",
};

function normalizeCategory(cat: string): string {
  const lower = (cat || "").toLowerCase().trim();
  if (lower.includes("rice") || lower === "c_rice") return "c_rice";
  if (lower.includes("oil") || lower === "c_oil") return "c_oil";
  if (lower.includes("dal") || lower.includes("pulse") || lower === "c_dal") return "c_dal";
  if (lower.includes("dairy") || lower.includes("milk") || lower.includes("curd") || lower.includes("ghee") || lower.includes("paneer") || lower === "c_dairy") return "c_dairy";
  if (lower.includes("spice") || lower.includes("masala") || lower === "c_spices") return "c_spices";
  if (lower.includes("snack") || lower.includes("biscuit") || lower.includes("beverage") || lower.includes("drink") || lower.includes("tea") || lower.includes("coffee") || lower.includes("noodle") || lower === "c_snacks") return "c_snacks";
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
    name: "update_product_details",
    description:
      "Update a product's details (name, Telugu name, brand, category, description, Telugu description, image, or listing visibility). Uploads new images directly to Cloudinary.",
    inputSchema: {
      type: "object",
      properties: {
        productId: {
          type: "string",
          description: "The unique ID of the product (e.g., 'p_rice_sona')",
        },
        name: {
          type: "string",
          description: "Optional new English display name",
        },
        nameTe: {
          type: "string",
          description: "Optional new Telugu display name (e.g., 'లలిత హెచ్.ఎమ్.టి రైస్')",
        },
        brand: {
          type: "string",
          description: "Optional brand name (e.g., 'Lalitha', 'Heritage', 'Amul', 'Fortune')",
        },
        category: {
          type: "string",
          description: "Optional new category: 'Dairy Essentials', 'Rice Bags', 'Cooking Oils', 'Dals & Pulses', or 'Spices & Masalas'",
        },
        description: {
          type: "string",
          description: "Optional new English description",
        },
        descriptionTe: {
          type: "string",
          description: "Optional new Telugu description",
        },
        imageUrl: {
          type: "string",
          description: "Optional new image URL. Automatically uploaded and optimized in Cloudinary folder 'ricemart_products'.",
        },
        isListed: {
          type: "boolean",
          description: "Optional visibility toggle. Set false to unlist/hide from the customer app.",
        },
      },
      required: ["productId"],
    },
  },
  {
    name: "generate_and_upload_product_image",
    description:
      "Generate a photo-realistic, studio e-commerce grocery product image using AI or process a reference photo taken by the store owner, automatically formatting it to a 600x600 square on a clean white background and uploading it to Cloudinary. Optionally attaches it directly to a product.",
    inputSchema: {
      type: "object",
      properties: {
        productName: {
          type: "string",
          description: "Name of the product (e.g. 'Lalitha Brand HMT Rice 26kg bag', 'Freedom Refined Sunflower Oil 1L')",
        },
        productId: {
          type: "string",
          description: "Optional product ID (e.g. 'p_rice_sona') to immediately update and attach this image in AWS AppSync.",
        },
        referenceImageUrlOrBase64: {
          type: "string",
          description: "Optional reference photo URL or base64 image taken on phone. If provided, uploads and formats this reference image.",
        },
        customPrompt: {
          type: "string",
          description: "Optional custom prompt describing visual packaging details (e.g. 'yellow woven bag with red Lalitha brand logo on pure white background, studio lighting')",
        },
        category: {
          type: "string",
          description: "Optional product category ('Rice Bags', 'Cooking Oils', 'Dals & Pulses', 'Dairy Essentials', 'Spices & Masalas')",
        },
      },
      required: ["productName"],
    },
  },
  {
    name: "add_product_variant",
    description:
      "Add a new weight or volume size variant (e.g. 5kg, 10kg, 26kg, 500ml, 1L, 200g) under an existing product with selling price, MRP, and stock.",
    inputSchema: {
      type: "object",
      properties: {
        productId: {
          type: "string",
          description: "The unique ID of the product (e.g., 'p_rice_sona')",
        },
        size: {
          type: "string",
          description: "Weight or volume size string (e.g., '5kg', '10kg', '26kg', '500ml', '1L', '200g')",
        },
        price: {
          type: "number",
          description: "Selling price in ₹ for this new size variant",
        },
        mrp: {
          type: "number",
          description: "Optional printed MRP in ₹ (defaults to ~15% above selling price)",
        },
        stock: {
          type: "number",
          description: "Initial stock count for this size variant (default 50)",
        },
      },
      required: ["productId", "size", "price"],
    },
  },
  {
    name: "delete_product_variant",
    description:
      "Remove a specific size variant (e.g., remove the 5kg option) from a product listing.",
    inputSchema: {
      type: "object",
      properties: {
        productId: {
          type: "string",
          description: "The unique ID of the product (e.g., 'p_rice_sona')",
        },
        variantSize: {
          type: "string",
          description: "The size/weight of the variant to delete (e.g., '5kg', '10kg', '500ml')",
        },
      },
      required: ["productId", "variantSize"],
    },
  },
  {
    name: "bulk_update_stock_or_prices",
    description:
      "Perform batch/bulk updates to stock or prices across multiple products/variants in a single operation. Perfect for restocking wholesale deliveries or updating prices across multiple items.",
    inputSchema: {
      type: "object",
      properties: {
        updates: {
          type: "array",
          items: {
            type: "object",
            properties: {
              productId: { type: "string" },
              variantSize: { type: "string", description: "Optional specific variant size (e.g. '26kg'). If omitted, updates all variants of the product." },
              newPrice: { type: "number", description: "Optional new selling price in ₹" },
              stock: { type: "number", description: "Optional absolute stock quantity" },
              addStock: { type: "number", description: "Optional stock increment (e.g. +50 bags to add to current stock)" },
            },
            required: ["productId"],
          },
          description: "List of product updates to apply in batch",
        },
      },
      required: ["updates"],
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
    name: "get_sales_report",
    description:
      "Generate a detailed sales and revenue report for a specified date range (e.g. today, yesterday, last 7 days, or between two dates). Computes total revenue, completed/pending/cancelled orders, average order value, and top selling products during that period.",
    inputSchema: {
      type: "object",
      properties: {
        startDate: {
          type: "string",
          description: "Start date (YYYY-MM-DD or ISO timestamp, e.g. '2026-08-01'). Defaults to 7 days ago if omitted.",
        },
        endDate: {
          type: "string",
          description: "End date (YYYY-MM-DD or ISO timestamp, e.g. '2026-08-29'). Defaults to current time if omitted.",
        },
        category: {
          type: "string",
          description: "Optional product category filter (e.g. 'Rice Bags', 'Cooking Oils', 'Dairy Essentials')",
        },
      },
    },
  },
  {
    name: "export_orders_csv",
    description:
      "Export customer orders within a date range to a CSV string suitable for spreadsheets, accounting, or tax tallying.",
    inputSchema: {
      type: "object",
      properties: {
        startDate: {
          type: "string",
          description: "Optional start date filter (YYYY-MM-DD)",
        },
        endDate: {
          type: "string",
          description: "Optional end date filter (YYYY-MM-DD)",
        },
        status: {
          type: "string",
          description: "Optional status filter (e.g. 'DELIVERED', 'PENDING', 'ALL')",
        },
      },
    },
  },
  {
    name: "set_store_availability",
    description:
      "Pause or resume incoming customer orders by marking the store as Open or Closed with an optional public reason (e.g. heavy rain, power outage, festival holiday, inventory counting).",
    inputSchema: {
      type: "object",
      properties: {
        isOpen: {
          type: "boolean",
          description: "Set true to accept orders, false to pause incoming orders",
        },
        closingReason: {
          type: "string",
          description: "Optional explanation shown to customers (e.g., 'Heavy rain in Rajam — delivery paused until 4 PM')",
        },
      },
      required: ["isOpen"],
    },
  },
  {
    name: "create_order",
    description:
      "Create and record a counter, phone, or WhatsApp customer order directly into G-Store's AWS backend. Calculates totals and automatically deducts stock.",
    inputSchema: {
      type: "object",
      properties: {
        customerName: {
          type: "string",
          description: "Customer full name (e.g. 'Ramesh', 'Sita Devi')",
        },
        customerPhone: {
          type: "string",
          description: "Customer 10-digit mobile number (e.g. '9876543210')",
        },
        deliveryAddress: {
          type: "string",
          description: "Delivery address or landmark in Rajam (e.g. 'D.No 4-12, Near SBI Bank, Rajam')",
        },
        items: {
          type: "array",
          items: {
            type: "object",
            properties: {
              productIdOrName: {
                type: "string",
                description: "Product ID (e.g. 'p_rice_sona') or product name keywords (e.g. 'Lalitha Rice', 'Heritage Milk')",
              },
              variantSize: {
                type: "string",
                description: "Optional pack size (e.g. '26kg', '10kg', '500ml'). Defaults to first variant if omitted.",
              },
              quantity: {
                type: "number",
                description: "Number of units/bags ordered",
              },
              price: {
                type: "number",
                description: "Optional custom unit price in ₹ (defaults to current catalog price)",
              },
            },
            required: ["productIdOrName", "quantity"],
          },
          description: "List of items ordered",
        },
        status: {
          type: "string",
          enum: ["PENDING", "PREPARING", "OUT_FOR_DELIVERY", "DELIVERED"],
          description: "Initial order status (default is PENDING)",
        },
        deliveryFee: {
          type: "number",
          description: "Delivery charge in ₹ (default 0 for free delivery)",
        },
      },
      required: ["customerName", "customerPhone", "deliveryAddress", "items"],
    },
  },
  {
    name: "generate_order_invoice",
    description:
      "Generate a clean, formatted receipt and packing slip for an order (formatted for printing or copying directly to WhatsApp for the customer or delivery agent).",
    inputSchema: {
      type: "object",
      properties: {
        orderId: {
          type: "string",
          description: "The order ID to generate invoice for (e.g. 'G-1786896453001-823')",
        },
      },
      required: ["orderId"],
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
      "Get the active store settings such as delivery radius (in km), minimum order value (in ₹), and store open/closed status.",
    inputSchema: {
      type: "object",
      properties: {},
    },
  },
  {
    name: "update_store_settings",
    description:
      "Update store operational settings such as delivery radius (km), minimum order amount (₹), or store open/closed status.",
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
        isStoreOpen: {
          type: "boolean",
          description: "Set store open/closed status",
        },
        closingReason: {
          type: "string",
          description: "Reason if store is closed",
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

    case "update_product_details": {
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

      // Parse existing description format: "${brand} ::: ${nameTe} ::: ${shortEn} ::: ${shortTe} ::: ${descEn} ::: ${descTe} ::: ${isListed}"
      const descParts = (product.description || "").split(" ::: ");
      const oldBrand = descParts[0] || "";
      const oldNameTe = descParts[1] || "";
      const oldShortEn = descParts[2] || product.name;
      const oldShortTe = descParts[3] || "";
      const oldDescEn = descParts[4] || "";
      const oldDescTe = descParts[5] || "";
      const oldIsListed = descParts.length > 6 ? descParts[6] !== "false" : true;

      const brand = args.brand !== undefined ? String(args.brand).trim() : oldBrand;
      const nameTe = args.nameTe !== undefined ? String(args.nameTe).trim() : oldNameTe;
      const descEn = args.description !== undefined ? String(args.description).trim() : oldDescEn;
      const descTe = args.descriptionTe !== undefined ? String(args.descriptionTe).trim() : oldDescTe;
      const isListed = args.isListed !== undefined ? Boolean(args.isListed) : oldIsListed;
      const newName = args.name !== undefined ? String(args.name).trim() : product.name;
      const shortEn = newName;
      const shortTe = nameTe || oldShortTe;

      const updatedDescString = `${brand} ::: ${nameTe} ::: ${shortEn} ::: ${shortTe} ::: ${descEn} ::: ${descTe} ::: ${isListed}`;

      let updatedImageUrls = product.imageUrls || [];
      if (args.imageUrl) {
        const finalUrl = await uploadToCloudinary(args.imageUrl);
        updatedImageUrls = [finalUrl];
      }

      const updateMutation = `
        mutation UpdateProduct($input: UpdateProductInput!) {
          updateProduct(input: $input) {
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

      const input: any = {
        id: product.id,
        name: newName,
        description: updatedDescString,
        imageUrls: updatedImageUrls,
      };

      if (args.category) {
        input.category = normalizeCategory(args.category);
      }

      const updateResult = await executeGraphQL(updateMutation, { input });
      const updated = updateResult?.updateProduct;

      return {
        message: "Product details updated successfully",
        product: {
          id: updated?.id,
          name: updated?.name,
          category: categoryNames[updated?.category] || updated?.category,
          description: updated?.description,
          isListed,
          imageUrls: updated?.imageUrls || [],
          variants: formatVariants(updated?.variants),
        },
      };
    }

    case "generate_and_upload_product_image": {
      const productName = String(args.productName || "").trim();
      const customPrompt = args.customPrompt ? String(args.customPrompt).trim() : "";
      const category = args.category ? String(args.category).trim() : "Grocery";
      const referenceImage = args.referenceImageUrlOrBase64 ? String(args.referenceImageUrlOrBase64).trim() : "";
      const productId = args.productId ? String(args.productId).trim() : "";

      let finalCloudinaryUrl = "";

      if (referenceImage) {
        // Case 1: Reference photo taken on phone or image URL provided
        finalCloudinaryUrl = await uploadToCloudinary(referenceImage);
      } else {
        // Case 2: AI-generated commercial studio packaging photo filling full card frame
        const promptDetails = customPrompt || `${productName}, authentic Indian grocery retail package, vibrant high-contrast packaging`;
        const fullPrompt = `Close-up commercial retail packaging photography of ${promptDetails}, ${category}, product packaging filling the full frame edge-to-edge, ultra-high resolution 8k details, vibrant crisp branding, studio lighting, clean background, modern quick-commerce product image`;
        
        const seed = Math.floor(Math.random() * 100000);
        const aiImageUrl = `https://image.pollinations.ai/prompt/${encodeURIComponent(fullPrompt)}?width=800&height=800&nologo=true&enhance=true&seed=${seed}`;
        
        finalCloudinaryUrl = await uploadToCloudinary(aiImageUrl);
      }

      // If productId is provided, automatically update the product in AWS AppSync
      let updatedProductInfo: any = null;
      if (productId) {
        try {
          const getQuery = `
            query GetProduct($id: ID!) {
              getProduct(id: $id) {
                id
                name
                imageUrls
              }
            }
          `;
          const existingData = await executeGraphQL(getQuery, { id: productId });
          const product = existingData?.getProduct;
          if (product) {
            const updateMutation = `
              mutation UpdateProduct($input: UpdateProductInput!) {
                updateProduct(input: $input) {
                  id
                  name
                  imageUrls
                }
              }
            `;
            const updateRes = await executeGraphQL(updateMutation, {
              input: {
                id: product.id,
                imageUrls: [finalCloudinaryUrl],
              },
            });
            updatedProductInfo = updateRes?.updateProduct;
          }
        } catch (err: any) {
          console.warn("Failed to attach image to productId:", err.message);
        }
      }

      return {
        message: updatedProductInfo
          ? `Image generated and successfully attached to product '${updatedProductInfo.name}' (${productId}) in G-Store catalog`
          : `Image generated and successfully uploaded to Cloudinary CDN`,
        cloudinaryImageUrl: finalCloudinaryUrl,
        productId: productId || undefined,
        productName: productName || undefined,
        modeUsed: referenceImage ? "Reference Photo Format & Square 600x600 Pad" : "AI Studio E-Commerce Generation",
      };
    }

    case "add_product_variant": {
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

      const rawSizeInput = String(args.size).trim();
      const sizeMatch = rawSizeInput.match(/^(\d+(?:\.\d+)?)\s*([a-zA-Z]+)$/);
      const unitVal = sizeMatch ? sizeMatch[1] : rawSizeInput;
      const unitType = sizeMatch ? sizeMatch[2] : (product.category === "c_rice" ? "kg" : "unit");

      const sellingPrice = Number(args.price);
      const mrpPrice = args.mrp ? Number(args.mrp) : Math.round(sellingPrice * 1.15);
      const stockCount = args.stock !== undefined ? Number(args.stock) : 50;

      const slug = (product.name || "").toLowerCase().replace(/[^a-z0-9]+/g, "_").slice(0, 15);
      const variantId = `v_${product.id}_${unitVal}${unitType}_${Date.now().toString().slice(-4)}`;
      const sku = `SKU-${(product.category || "GEN").toUpperCase().replace("C_", "")}-${slug.toUpperCase()}-${unitVal}${unitType.toUpperCase()}`;
      const variantSizeString = `${unitVal}:::${unitType}:::${mrpPrice.toFixed(1)}:::${sku}:::${variantId}`;

      const existingVariants: any[] = product.variants || [];
      const matchIndex = existingVariants.findIndex((v: any) => {
        const parts = (v.size || "").split(":::");
        if (parts.length >= 2) {
          return parts[0].toLowerCase() === unitVal.toLowerCase() && parts[1].toLowerCase() === unitType.toLowerCase();
        }
        return v.size.toLowerCase().includes(rawSizeInput.toLowerCase());
      });

      let updatedVariants = [...existingVariants];
      if (matchIndex >= 0) {
        updatedVariants[matchIndex] = {
          ...updatedVariants[matchIndex],
          size: variantSizeString,
          price: sellingPrice,
          stock: stockCount,
        };
      } else {
        updatedVariants.push({
          size: variantSizeString,
          price: sellingPrice,
          stock: stockCount,
        });
      }

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
        message: matchIndex >= 0 ? `Updated existing variant ${rawSizeInput} for ${product.name}` : `Added new variant ${rawSizeInput} to ${product.name}`,
        product: {
          id: updateResult?.updateProduct?.id,
          name: updateResult?.updateProduct?.name,
          variants: formatVariants(updateResult?.updateProduct?.variants),
        },
      };
    }

    case "delete_product_variant": {
      const getQuery = `
        query GetProduct($id: ID!) {
          getProduct(id: $id) {
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
      const existingData = await executeGraphQL(getQuery, { id: args?.productId });
      const product = existingData?.getProduct;

      if (!product) {
        throw new Error(`Product with ID ${args?.productId} not found.`);
      }

      const currentVariants: any[] = product.variants || [];
      if (currentVariants.length <= 1) {
        return {
          error: "Cannot delete the only remaining variant of this product. Use delete_product to remove the whole product listing.",
        };
      }

      const targetSize = String(args.variantSize).toLowerCase().trim();
      const remaining = currentVariants.filter((v: any) => {
        const parts = (v.size || "").split(":::");
        const weight = parts.length >= 2 ? `${parts[0]}${parts[1]}`.toLowerCase() : v.size.toLowerCase();
        return !weight.includes(targetSize) && !v.size.toLowerCase().includes(targetSize);
      });

      if (remaining.length === currentVariants.length) {
        return {
          message: `No variant found matching '${args.variantSize}'. Current variants: ${currentVariants.map((v) => v.size).join(", ")}`,
        };
      }

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
          variants: remaining,
        },
      });

      return {
        message: `Variant '${args.variantSize}' removed successfully from ${product.name}`,
        product: {
          id: updateResult?.updateProduct?.id,
          name: updateResult?.updateProduct?.name,
          variants: formatVariants(updateResult?.updateProduct?.variants),
        },
      };
    }

    case "bulk_update_stock_or_prices": {
      const updates = Array.isArray(args?.updates) ? args.updates : [];
      if (updates.length === 0) {
        return { message: "No updates provided" };
      }

      const results: any[] = [];
      for (const item of updates) {
        try {
          const getQuery = `
            query GetProduct($id: ID!) {
              getProduct(id: $id) {
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
          const existingData = await executeGraphQL(getQuery, { id: item.productId });
          const product = existingData?.getProduct;
          if (!product) {
            results.push({ productId: item.productId, status: "ERROR", error: "Product not found" });
            continue;
          }

          let modified = false;
          const updatedVariants = (product.variants || []).map((v: any) => {
            const isMatch =
              !item.variantSize ||
              v.size.toLowerCase().includes(String(item.variantSize).toLowerCase());

            if (isMatch) {
              modified = true;
              let newStock = v.stock;
              if (item.stock !== undefined) {
                newStock = Number(item.stock);
              } else if (item.addStock !== undefined) {
                newStock = Math.max(0, v.stock + Number(item.addStock));
              }

              const newPrice = item.newPrice !== undefined ? Number(item.newPrice) : v.price;
              return { ...v, price: newPrice, stock: newStock };
            }
            return v;
          });

          if (modified) {
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
            await executeGraphQL(updateMutation, {
              input: { id: product.id, variants: updatedVariants },
            });
            results.push({
              productId: product.id,
              productName: product.name,
              status: "UPDATED",
              updatedVariants: formatVariants(updatedVariants),
            });
          } else {
            results.push({
              productId: product.id,
              productName: product.name,
              status: "SKIPPED",
              reason: `No variant matched size '${item.variantSize}'`,
            });
          }
        } catch (err: any) {
          results.push({ productId: item.productId, status: "ERROR", error: err.message });
        }
      }

      return {
        totalRequested: updates.length,
        totalUpdated: results.filter((r) => r.status === "UPDATED").length,
        results,
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

    case "get_sales_report": {
      const query = `
        query ListOrders {
          listOrders(limit: 1000) {
            items {
              id
              customerId
              customerName
              deliveryAddress
              deliveryFee
              subtotal
              total
              status
              createdAt
              items {
                productId
                productName
                quantity
                variantSize
                price
              }
            }
          }
        }
      `;
      const data = await executeGraphQL(query);
      const allOrders: any[] = data?.listOrders?.items || [];

      // Determine date window
      const now = new Date();
      const defaultStart = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
      const start = args?.startDate ? new Date(args.startDate) : defaultStart;
      const end = args?.endDate ? new Date(args.endDate + "T23:59:59.999Z") : now;

      const filteredOrders = allOrders.filter((o) => {
        if (!o.createdAt) return true;
        const orderDate = new Date(o.createdAt);
        return orderDate >= start && orderDate <= end;
      });

      let totalGrossRevenue = 0;
      let completedOrdersCount = 0;
      let pendingOrdersCount = 0;
      let cancelledOrdersCount = 0;
      const itemSalesMap: Record<string, { name: string; quantity: number; revenue: number }> = {};
      const dailyMap: Record<string, { orders: number; revenue: number }> = {};

      for (const o of filteredOrders) {
        const status = (o.status || "PENDING").toUpperCase();
        if (status === "CANCELLED") {
          cancelledOrdersCount++;
          continue;
        }

        if (status === "DELIVERED") {
          completedOrdersCount++;
        } else {
          pendingOrdersCount++;
        }

        const total = Number(o.total) || 0;
        totalGrossRevenue += total;

        const dayKey = o.createdAt ? o.createdAt.slice(0, 10) : "Recent";
        if (!dailyMap[dayKey]) {
          dailyMap[dayKey] = { orders: 0, revenue: 0 };
        }
        dailyMap[dayKey].orders++;
        dailyMap[dayKey].revenue += total;

        for (const it of o.items || []) {
          const key = it.productId || it.productName || "Unknown Item";
          if (!itemSalesMap[key]) {
            itemSalesMap[key] = {
              name: it.productName || key,
              quantity: 0,
              revenue: 0,
            };
          }
          const qty = Number(it.quantity) || 1;
          const price = Number(it.price) || 0;
          itemSalesMap[key].quantity += qty;
          itemSalesMap[key].revenue += qty * price;
        }
      }

      const topProducts = Object.values(itemSalesMap)
        .sort((a, b) => b.revenue - a.revenue)
        .slice(0, 10)
        .map((p) => ({
          name: p.name,
          unitsSold: p.quantity,
          revenue: `₹${p.revenue.toFixed(0)}`,
        }));

      const activeOrdersCount = completedOrdersCount + pendingOrdersCount;
      const averageOrderValue =
        activeOrdersCount > 0 ? (totalGrossRevenue / activeOrdersCount).toFixed(0) : "0";

      return {
        dateRange: {
          start: start.toISOString().slice(0, 10),
          end: end.toISOString().slice(0, 10),
        },
        financialSummary: {
          totalGrossRevenue: `₹${totalGrossRevenue.toLocaleString("en-IN")}`,
          totalRevenueNumber: totalGrossRevenue,
          averageOrderValue: `₹${averageOrderValue}`,
          totalOrdersPlaced: filteredOrders.length,
          deliveredOrders: completedOrdersCount,
          pendingOrders: pendingOrdersCount,
          cancelledOrders: cancelledOrdersCount,
        },
        dailyBreakdown: Object.entries(dailyMap).map(([day, val]) => ({
          date: day,
          orders: val.orders,
          revenue: `₹${val.revenue.toFixed(0)}`,
        })),
        topSellingProducts: topProducts,
      };
    }

    case "export_orders_csv": {
      const query = `
        query ListOrders {
          listOrders(limit: 1000) {
            items {
              id
              customerId
              customerName
              deliveryAddress
              deliveryFee
              subtotal
              total
              status
              createdAt
              items {
                productId
                productName
                quantity
                variantSize
                price
              }
            }
          }
        }
      `;
      const data = await executeGraphQL(query);
      let orders: any[] = data?.listOrders?.items || [];

      if (args?.status && args.status !== "ALL") {
        orders = orders.filter((o) => (o.status || "").toUpperCase() === args.status.toUpperCase());
      }
      if (args?.startDate) {
        const start = new Date(args.startDate);
        orders = orders.filter((o) => !o.createdAt || new Date(o.createdAt) >= start);
      }
      if (args?.endDate) {
        const end = new Date(args.endDate + "T23:59:59.999Z");
        orders = orders.filter((o) => !o.createdAt || new Date(o.createdAt) <= end);
      }

      // Generate CSV
      const headers = [
        "Order ID",
        "Date",
        "Customer Name",
        "Customer Phone",
        "Status",
        "Item Count",
        "Subtotal",
        "Delivery Fee",
        "Total Amount",
        "Delivery Address",
        "Items Ordered",
      ];

      const csvRows = [headers.join(",")];

      for (const o of orders) {
        const addrParts = (o.deliveryAddress || "").split(":::");
        const addrText = (addrParts[1] || o.deliveryAddress || "").replace(/"/g, '""');
        const phone = addrParts[2] || "";
        const dateStr = o.createdAt ? o.createdAt.slice(0, 10) : "";
        const itemsSummary = (o.items || [])
          .map((it: any) => `${it.productName} (${it.variantSize || ""}) x${it.quantity}`)
          .join(" | ")
          .replace(/"/g, '""');

        const row = [
          `"${o.id}"`,
          `"${dateStr}"`,
          `"${(o.customerName || "Customer").replace(/"/g, '""')}"`,
          `"${phone}"`,
          `"${o.status}"`,
          (o.items || []).reduce((acc: number, cur: any) => acc + (cur.quantity || 1), 0),
          o.subtotal || o.total,
          o.deliveryFee || 0,
          o.total,
          `"${addrText}"`,
          `"${itemsSummary}"`,
        ];
        csvRows.push(row.join(","));
      }

      const csvContent = csvRows.join("\n");

      return {
        totalOrdersExported: orders.length,
        csvFileName: `gstore_orders_${Date.now()}.csv`,
        csvContent,
      };
    }

    case "set_store_availability": {
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

      let currentConfig: any = { minimumOrderAmount: 150.0, deliveryRadiusKm: 10.0, isStoreOpen: true, closingReason: "" };
      if (existing && existing.description) {
        try {
          currentConfig = JSON.parse(existing.description);
        } catch {
          // fallback
        }
      }

      const isStoreOpen = Boolean(args?.isOpen);
      const closingReason = args?.closingReason !== undefined ? String(args.closingReason) : (isStoreOpen ? "" : (currentConfig.closingReason || "Store currently closed"));

      const updatedConfig = {
        ...currentConfig,
        isStoreOpen,
        closingReason,
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
        message: isStoreOpen
          ? "Store is now OPEN and accepting customer orders!"
          : `Store is now CLOSED. Notice: "${closingReason}"`,
        storeStatus: {
          isStoreOpen,
          closingReason,
        },
      };
    }

    case "create_order": {
      const customerName = String(args.customerName || "Counter Customer").trim();
      const customerPhone = String(args.customerPhone || "").trim();
      const rawAddress = String(args.deliveryAddress || "Counter Pickup, Rajam").trim();
      const status = args.status || "PENDING";
      const deliveryFee = args.deliveryFee !== undefined ? Number(args.deliveryFee) : 0;

      // Fetch products to match IDs & prices
      const prodsQuery = `
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
      const prodData = await executeGraphQL(prodsQuery);
      const allProds: any[] = (prodData?.listProducts?.items || []).filter(
        (p: any) => !p.id.startsWith("sys_") && p.category !== "metadata"
      );

      const itemsInput: any[] = [];
      let subtotal = 0;
      const stockUpdatesToPerform: any[] = [];

      for (const itemArg of args.items || []) {
        const searchKey = String(itemArg.productIdOrName || "").toLowerCase().trim();
        const matchedProd = allProds.find(
          (p) => p.id.toLowerCase() === searchKey || p.name.toLowerCase().includes(searchKey)
        );

        const qty = Number(itemArg.quantity) || 1;
        let unitPrice = itemArg.price !== undefined ? Number(itemArg.price) : 0;
        let variantSize = itemArg.variantSize || "";
        let prodId = matchedProd ? matchedProd.id : `p_custom_${Date.now().toString().slice(-4)}`;
        let prodName = matchedProd ? matchedProd.name : itemArg.productIdOrName;

        if (matchedProd && matchedProd.variants && matchedProd.variants.length > 0) {
          const variants = matchedProd.variants;
          let matchedVar = variants[0];
          if (itemArg.variantSize) {
            const varSearch = String(itemArg.variantSize).toLowerCase();
            const found = variants.find((v: any) => v.size.toLowerCase().includes(varSearch));
            if (found) matchedVar = found;
          }
          if (unitPrice === 0) {
            unitPrice = matchedVar.price;
          }
          if (!variantSize) {
            const parts = (matchedVar.size || "").split(":::");
            variantSize = parts.length >= 2 ? `${parts[0]} ${parts[1]}` : matchedVar.size;
          }

          stockUpdatesToPerform.push({
            product: matchedProd,
            variant: matchedVar,
            deductQty: qty,
          });
        }

        subtotal += unitPrice * qty;
        itemsInput.push({
          productId: prodId,
          productName: prodName,
          quantity: qty,
          price: unitPrice,
          variantSize: variantSize || "Standard",
        });
      }

      const totalAmount = subtotal + deliveryFee;
      const orderId = `G-${Date.now()}-${Math.floor(100 + Math.random() * 900)}`;
      const deliveryAddressCombined = `${rawAddress} ::: Rajam ::: ${customerPhone} ::: 1.0`;

      const createOrderMutation = `
        mutation CreateOrder($input: CreateOrderInput!) {
          createOrder(input: $input) {
            id
            customerId
            customerName
            deliveryAddress
            total
            status
            createdAt
          }
        }
      `;

      const orderInput = {
        id: orderId,
        customerId: `cust_${customerPhone || Date.now()}`,
        customerName,
        deliveryAddress: deliveryAddressCombined,
        deliveryFee,
        latitude: 18.4482,
        longitude: 83.6616,
        status,
        subtotal,
        total: totalAmount,
        items: itemsInput,
      };

      await executeGraphQL(createOrderMutation, { input: orderInput });

      // Deduct stock for ordered items
      for (const st of stockUpdatesToPerform) {
        try {
          const updatedVars = (st.product.variants || []).map((v: any) => {
            if (v.size === st.variant.size) {
              return { ...v, stock: Math.max(0, v.stock - st.deductQty) };
            }
            return v;
          });
          const updateMutation = `
            mutation UpdateProduct($input: UpdateProductInput!) {
              updateProduct(input: $input) {
                id
                variants {
                  size
                  price
                  stock
                }
              }
            }
          `;
          await executeGraphQL(updateMutation, { input: { id: st.product.id, variants: updatedVars } });
        } catch {
          // non-critical
        }
      }

      return {
        message: `Order #${orderId} recorded successfully for ${customerName}`,
        order: {
          orderId,
          customerName,
          customerPhone,
          deliveryAddress: rawAddress,
          status,
          subtotal: `₹${subtotal}`,
          deliveryFee: `₹${deliveryFee}`,
          totalAmount: `₹${totalAmount}`,
          items: itemsInput.map((it) => ({
            product: it.productName,
            size: it.variantSize,
            qty: it.quantity,
            price: `₹${it.price}`,
            lineTotal: `₹${it.price * it.quantity}`,
          })),
        },
      };
    }

    case "generate_order_invoice": {
      const query = `
        query GetOrder($id: ID!) {
          getOrder(id: $id) {
            id
            customerId
            customerName
            deliveryAddress
            deliveryFee
            subtotal
            total
            status
            createdAt
            items {
              productId
              productName
              quantity
              variantSize
              price
            }
          }
        }
      `;
      const data = await executeGraphQL(query, { id: args?.orderId });
      const order = data?.getOrder;

      if (!order) {
        throw new Error(`Order with ID ${args?.orderId} not found.`);
      }

      const formatted = formatOrder(order);
      const itemsListText = (formatted.items || [])
        .map(
          (it: any, i: number) =>
            `${i + 1}. ${it.productName} (${it.size || "1 unit"}) x ${it.quantity} = ${it.price}`
        )
        .join("\n");

      const invoiceText = `
========================================
         🏪 G-STORE / RICE MART
        Main Road, Rajam, A.P.
        📞 +91 9704173515
========================================
Order ID    : ${formatted.orderId}
Date        : ${formatted.createdAt ? new Date(formatted.createdAt).toLocaleString("en-IN") : "Recent"}
Customer    : ${formatted.customerName}
Phone       : ${formatted.customerPhone || "N/A"}
Address     : ${formatted.deliveryAddress}
Status      : ${formatted.status}
----------------------------------------
ITEMS ORDERED:
${itemsListText}
----------------------------------------
Subtotal    : ${formatted.subtotal || formatted.total}
Delivery Fee: ${formatted.deliveryFee || "FREE (₹0)"}
TOTAL DUE   : ${formatted.total} (Cash on Delivery / UPI)
========================================
Thank you for shopping at G-Store, Rajam!
`.trim();

      return {
        orderId: formatted.orderId,
        customerName: formatted.customerName,
        customerPhone: formatted.customerPhone,
        total: formatted.total,
        status: formatted.status,
        printableInvoice: invoiceText,
        whatsAppShareText: invoiceText,
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

      let currentConfig: any = { minimumOrderAmount: 150.0, deliveryRadiusKm: 10.0, isStoreOpen: true, closingReason: "" };
      if (existing && existing.description) {
        try {
          currentConfig = JSON.parse(existing.description);
        } catch {
          // fallback
        }
      }

      const updatedConfig = {
        ...currentConfig,
        minimumOrderAmount:
          args?.minimumOrderAmount !== undefined ? args.minimumOrderAmount : currentConfig.minimumOrderAmount,
        deliveryRadiusKm:
          args?.deliveryRadiusKm !== undefined ? args.deliveryRadiusKm : currentConfig.deliveryRadiusKm,
        isStoreOpen:
          args?.isStoreOpen !== undefined ? Boolean(args.isStoreOpen) : currentConfig.isStoreOpen,
        closingReason:
          args?.closingReason !== undefined ? String(args.closingReason) : currentConfig.closingReason,
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
          isStoreOpen: updatedConfig.isStoreOpen,
          closingReason: updatedConfig.closingReason,
          formatted: {
            minimumOrder: `₹${updatedConfig.minimumOrderAmount}`,
            deliveryRadius: `${updatedConfig.deliveryRadiusKm} km`,
            status: updatedConfig.isStoreOpen ? "🟢 OPEN" : "🔴 CLOSED",
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
