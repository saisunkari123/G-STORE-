import fetch from "node-fetch";
import dotenv from "dotenv";

dotenv.config();

const APPSYNC_ENDPOINT = process.env.APPSYNC_ENDPOINT || "";
const APPSYNC_API_KEY = process.env.APPSYNC_API_KEY || "";

export interface GraphQLResponse<T = any> {
  data?: T;
  errors?: Array<{ message: string }>;
}

export async function executeGraphQL<T = any>(
  query: string,
  variables: Record<string, any> = {}
): Promise<T> {
  if (!APPSYNC_ENDPOINT || !APPSYNC_API_KEY) {
    throw new Error("Missing APPSYNC_ENDPOINT or APPSYNC_API_KEY in environment variables");
  }

  const response = await fetch(APPSYNC_ENDPOINT, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "x-api-key": APPSYNC_API_KEY,
    },
    body: JSON.stringify({ query, variables }),
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(`AppSync HTTP Error: ${response.status} ${response.statusText} - ${text}`);
  }

  const json = (await response.json()) as GraphQLResponse<T>;
  if (json.errors && json.errors.length > 0) {
    throw new Error(`AppSync GraphQL Error: ${json.errors.map((e) => e.message).join(", ")}`);
  }

  return json.data as T;
}
