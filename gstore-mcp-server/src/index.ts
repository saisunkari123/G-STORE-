import express from "express";
import cors from "cors";
import dotenv from "dotenv";
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

app.use(express.json());

const PORT = process.env.PORT || 3000;

// G-Store MCP Tools List conforming to MCP Specification (2024-11-05)
const toolsList = [
  {
    name: "list_products",
    description:
      "Retrieve all products available in G-Store, including category, description, and variants (sizes like 5kg, 10kg, 25kg, prices, and stock status).",
    inputSchema: {
      type: "object",
      properties: {
        category: {
          type: "string",
          description: "Optional category filter (e.g., 'Rice Bags', 'Cooking Oils', 'Dals & Pulses')",
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
          description: "The unique ID of the product",
        },
      },
      required: ["productId"],
    },
  },
  {
    name: "update_product_price_or_stock",
    description:
      "Update the price or in-stock status of a specific product variant (e.g., set 25kg Basmati price to ₹1850 or set out-of-stock).",
    inputSchema: {
      type: "object",
      properties: {
        productId: {
          type: "string",
          description: "The product ID",
        },
        variantSize: {
          type: "string",
          description: "The variant size (e.g., '5kg', '10kg', '25kg')",
        },
        newPrice: {
          type: "number",
          description: "Optional new selling price",
        },
        inStock: {
          type: "boolean",
          description: "Optional stock availability flag",
        },
      },
      required: ["productId"],
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
    name: "update_order_status",
    description:
      "Update the fulfillment status of an order (e.g. advance from PENDING to PREPARING, OUT_FOR_DELIVERY, or DELIVERED).",
    inputSchema: {
      type: "object",
      properties: {
        orderId: {
          type: "string",
          description: "The ID of the order to update",
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
                mrp
                inStock
                weight
              }
            }
          }
        }
      `;
      const data = await executeGraphQL(query);
      let products = data?.listProducts?.items || [];

      if (args?.category) {
        const categoryFilter = String(args.category).toLowerCase();
        products = products.filter(
          (p: any) =>
            p.category && p.category.toLowerCase().includes(categoryFilter)
        );
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
              mrp
              inStock
              weight
            }
          }
        }
      `;
      const data = await executeGraphQL(query, { id: args?.productId });
      return data?.getProduct || { message: "Product not found" };
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
              mrp
              inStock
              weight
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
        if (!args?.variantSize || v.size === args.variantSize) {
          return {
            ...v,
            price: args?.newPrice !== undefined ? args.newPrice : v.price,
            inStock: args?.inStock !== undefined ? args.inStock : v.inStock,
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
              inStock
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

      return { message: "Product updated successfully", product: updateResult?.updateProduct };
    }

    case "list_orders": {
      const limit = typeof args?.limit === "number" ? args.limit : 50;
      const query = `
        query ListOrders($limit: Int) {
          listOrders(limit: $limit) {
            items {
              id
              userId
              orderStatus
              total
              subtotal
              deliveryFee
              createdAt
              items {
                name
                size
                quantity
                price
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
          (o: any) => o.orderStatus && o.orderStatus.toUpperCase() === targetStatus
        );
      }

      return { count: orders.length, orders };
    }

    case "update_order_status": {
      const mutation = `
        mutation UpdateOrder($input: UpdateOrderInput!) {
          updateOrder(input: $input) {
            id
            orderStatus
            updatedAt
          }
        }
      `;
      const result = await executeGraphQL(mutation, {
        input: {
          id: args?.orderId,
          orderStatus: args?.newStatus,
        },
      });

      return { message: "Order status updated", order: result?.updateOrder };
    }

    case "get_store_metrics": {
      const ordersQuery = `
        query ListOrders {
          listOrders(limit: 1000) {
            items {
              id
              orderStatus
              total
              createdAt
            }
          }
        }
      `;
      const data = await executeGraphQL(ordersQuery);
      const orders = data?.listOrders?.items || [];

      const totalOrders = orders.length;
      const pendingOrders = orders.filter((o: any) => o.orderStatus === "PENDING").length;
      const preparingOrders = orders.filter((o: any) => o.orderStatus === "PREPARING").length;
      const outForDelivery = orders.filter((o: any) => o.orderStatus === "OUT_FOR_DELIVERY").length;
      const deliveredOrders = orders.filter((o: any) => o.orderStatus === "DELIVERED").length;

      const totalRevenue = orders
        .filter((o: any) => o.orderStatus !== "CANCELLED")
        .reduce((sum: number, o: any) => sum + (Number(o.total) || 0), 0);

      return {
        metrics: {
          totalOrders,
          totalRevenue: `₹${totalRevenue.toFixed(2)}`,
          pendingOrders,
          preparingOrders,
          outForDelivery,
          deliveredOrders,
        },
      };
    }

    case "get_store_settings": {
      const query = `
        query ListAppConfigs {
          listAppConfigs(limit: 1) {
            items {
              id
              minimumOrderAmount
              deliveryRadiusKm
            }
          }
        }
      `;
      const data = await executeGraphQL(query);
      return data?.listAppConfigs?.items?.[0] || {
        minimumOrderAmount: 150.0,
        deliveryRadiusKm: 10.0,
      };
    }

    case "update_store_settings": {
      const listQuery = `
        query ListAppConfigs {
          listAppConfigs(limit: 1) {
            items {
              id
            }
          }
        }
      `;
      const listData = await executeGraphQL(listQuery);
      const existingId = listData?.listAppConfigs?.items?.[0]?.id || "default_config";

      const mutation = `
        mutation UpdateAppConfig($input: UpdateAppConfigInput!) {
          updateAppConfig(input: $input) {
            id
            minimumOrderAmount
            deliveryRadiusKm
          }
        }
      `;

      const updateData = await executeGraphQL(mutation, {
        input: {
          id: existingId,
          minimumOrderAmount: args?.minimumOrderAmount,
          deliveryRadiusKm: args?.deliveryRadiusKm,
        },
      });

      return { message: "Store settings updated", config: updateData?.updateAppConfig };
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
            version: "1.0.0",
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
    version: "1.0.0",
    protocolVersion: "2024-11-05",
    toolsCount: toolsList.length,
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
    version: "1.0.0",
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
  console.log(`🚀 G-Store MCP Server listening on http://localhost:${PORT}`);
  console.log(`📡 Clean MCP endpoints ready at /mcp, /sse, and /`);
});
