import express from "express";
import cors from "cors";
import dotenv from "dotenv";
import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StreamableHTTPServerTransport } from "@modelcontextprotocol/sdk/server/streamableHttp.js";
import {
  CallToolRequestSchema,
  ListToolsRequestSchema,
} from "@modelcontextprotocol/sdk/types.js";
import { executeGraphQL } from "./appsync.js";

dotenv.config();

const app = express();

// Full CORS & header support for Gemini Spark & remote MCP clients
app.use(
  cors({
    origin: "*",
    methods: ["GET", "POST", "OPTIONS", "HEAD"],
    allowedHeaders: ["Content-Type", "Authorization", "x-session-id", "mcp-session-id", "Accept"],
    exposedHeaders: ["x-session-id", "mcp-session-id"],
  })
);

app.use(express.json());

const PORT = process.env.PORT || 3000;

// Create MCP Server Instance
const server = new Server(
  {
    name: "gstore-mcp-server",
    version: "1.0.0",
  },
  {
    capabilities: {
      tools: {},
    },
  }
);

// Register Available Tools in MCP
server.setRequestHandler(ListToolsRequestSchema, async () => {
  return {
    tools: [
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
    ],
  };
});

// Handle Tool Executions
server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const { name, arguments: args } = request.params;

  try {
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

        return {
          content: [
            {
              type: "text",
              text: JSON.stringify({ count: products.length, products }, null, 2),
            },
          ],
        };
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
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(data?.getProduct || { message: "Product not found" }, null, 2),
            },
          ],
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
          return {
            content: [{ type: "text", text: `Product with ID ${args?.productId} not found.` }],
            isError: true,
          };
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

        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(
                { message: "Product updated successfully", product: updateResult?.updateProduct },
                null,
                2
              ),
            },
          ],
        };
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

        return {
          content: [
            {
              type: "text",
              text: JSON.stringify({ count: orders.length, orders }, null, 2),
            },
          ],
        };
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

        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(
                { message: "Order status updated", order: result?.updateOrder },
                null,
                2
              ),
            },
          ],
        };
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
          content: [
            {
              type: "text",
              text: JSON.stringify(
                {
                  metrics: {
                    totalOrders,
                    totalRevenue: `₹${totalRevenue.toFixed(2)}`,
                    pendingOrders,
                    preparingOrders,
                    outForDelivery,
                    deliveredOrders,
                  },
                },
                null,
                2
              ),
            },
          ],
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
        const config = data?.listAppConfigs?.items?.[0] || {
          minimumOrderAmount: 150.0,
          deliveryRadiusKm: 10.0,
        };

        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(config, null, 2),
            },
          ],
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

        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(
                { message: "Store settings updated", config: updateData?.updateAppConfig },
                null,
                2
              ),
            },
          ],
        };
      }

      default:
        return {
          content: [{ type: "text", text: `Unknown tool name: ${name}` }],
          isError: true,
        };
    }
  } catch (error: any) {
    return {
      content: [{ type: "text", text: `Error executing ${name}: ${error.message}` }],
      isError: true,
    };
  }
});

// Modern Streamable HTTP Transport for MCP
const transport = new StreamableHTTPServerTransport({
  sessionIdGenerator: undefined, // Stateless mode for maximum client compatibility
});

// Connect transport to MCP Server
await server.connect(transport);

// Universal handler for /mcp, /sse, and fallback endpoints
const mcpHandler = async (req: express.Request, res: express.Response) => {
  try {
    await transport.handleRequest(req, res, req.body);
  } catch (err: any) {
    console.error("Transport error handling request:", err);
    if (!res.headersSent) {
      res.status(500).json({ error: err.message });
    }
  }
};

app.all("/mcp", mcpHandler);
app.all("/sse", mcpHandler);
app.all("/mcp/message", mcpHandler);
app.all("/message", mcpHandler);

// Health check endpoint
app.get("/", (req, res) => {
  // If request asks for json or text/event-stream, pass to transport
  const accept = req.headers.accept || "";
  if (accept.includes("text/event-stream") || accept.includes("application/json-rpc")) {
    return mcpHandler(req, res);
  }

  res.json({
    status: "online",
    name: "G-Store MCP Server (Streamable HTTP)",
    version: "1.0.0",
    endpoints: {
      mcp: "/mcp",
      sse: "/sse",
    },
  });
});

app.post("/", mcpHandler);

app.listen(PORT, () => {
  console.log(`🚀 G-Store MCP Server listening on http://localhost:${PORT}`);
  console.log(`📡 Streamable MCP endpoints ready at /mcp, /sse, and /`);
});
