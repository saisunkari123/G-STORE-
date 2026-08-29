# G-Store MCP Server for Gemini Spark

This server implements the **Model Context Protocol (MCP)** to allow **Google Gemini Spark** to directly interact with your **G-Store (Rice Mart)** cloud backend.

## 🛠️ Available MCP Tools

1. **`list_products`**: Lists all products, categories, bag weights, prices, and stock indicators.
2. **`get_product_by_id`**: Fetches full details for a product.
3. **`update_product_price_or_stock`**: Allows updating prices or in/out-of-stock states for any variant.
4. **`list_orders`**: Lists customer orders with status filtering (`PENDING`, `PREPARING`, `OUT_FOR_DELIVERY`, `DELIVERED`).
5. **`update_order_status`**: Advances order fulfillment status.
6. **`get_store_metrics`**: Computes total sales revenue, order counts, and active order breakdown.
7. **`get_store_settings`**: Reads delivery radius (km) and minimum order value (₹).
8. **`update_store_settings`**: Configures delivery radius and minimum order value.

## 🚀 Running Locally

```bash
cd gstore-mcp-server
npm install
npm run dev
```

Server will start on `http://localhost:3000`.

## 🌐 Deploying with 1-Click Free HTTPS (for Gemini Spark)

### Option A: Render (Free Web Service)
1. Push this directory to your GitHub repository.
2. Go to [dashboard.render.com](https://dashboard.render.com) -> **New Web Service**.
3. Set **Root Directory** to `gstore-mcp-server`.
4. Set **Build Command** to `npm install && npm run build`.
5. Set **Start Command** to `npm start`.
6. Add Environment Variables:
   - `APPSYNC_ENDPOINT`: `https://gosrh7asubcb3i2qznf6niewd4.appsync-api.us-east-1.amazonaws.com/graphql`
   - `APPSYNC_API_KEY`: `da2-ko7gergfqrdslobaeu7jng6iiq`
7. Copy your deployed URL: `https://your-service.onrender.com/mcp` and paste it into Gemini Spark.
