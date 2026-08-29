import { executeGraphQL } from "../src/appsync.js";
import crypto from "crypto";
import dotenv from "dotenv";

dotenv.config();

const CLOUDINARY_CLOUD_NAME = process.env.CLOUDINARY_CLOUD_NAME || "k1lw675z";
const CLOUDINARY_API_KEY = process.env.CLOUDINARY_API_KEY || "498889461713286";
const CLOUDINARY_API_SECRET = process.env.CLOUDINARY_API_SECRET || "8SR-robZhuJf-5ehvJTFCscCatY";

function formatCloudinaryUrl(url: string): string {
  if (url && url.includes("res.cloudinary.com") && url.includes("/upload/")) {
    if (!url.includes("/upload/c_") && !url.includes("/upload/w_")) {
      return url.replace("/upload/", "/upload/c_fill,g_auto,w_600,h_400,f_auto,q_auto/");
    }
  }
  return url;
}

async function uploadToCloudinary(imageUrl: string, publicIdSuffix?: string): Promise<string> {
  if (!imageUrl || imageUrl.startsWith("android.resource://")) {
    return imageUrl;
  }

  if (imageUrl.includes(`res.cloudinary.com/${CLOUDINARY_CLOUD_NAME}`)) {
    return formatCloudinaryUrl(imageUrl);
  }

  try {
    const timestamp = Math.floor(Date.now() / 1000).toString();
    const folder = "ricemart_products";
    const signStr = `folder=${folder}&timestamp=${timestamp}${CLOUDINARY_API_SECRET}`;
    const signature = crypto.createHash("sha1").update(signStr).digest("hex");

    const formData = new URLSearchParams();
    formData.append("file", imageUrl);
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
    } else {
      console.warn(`Cloudinary warning for ${imageUrl}:`, data?.error?.message || JSON.stringify(data));
      return imageUrl;
    }
  } catch (err: any) {
    console.error(`Failed to upload ${imageUrl} to Cloudinary:`, err.message);
    return imageUrl;
  }
}

async function migrateAllImages() {
  console.log("=================================================");
  console.log("🚀 Starting Image Migration to Cloudinary & AWS...");
  console.log(`Cloud Name: ${CLOUDINARY_CLOUD_NAME}`);
  console.log("=================================================\n");

  const listQuery = `
    query ListAllProducts {
      listProducts {
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

  const data = await executeGraphQL(listQuery);
  const products = data?.listProducts?.items || [];
  console.log(`Found ${products.length} items in AWS AppSync DynamoDB.\n`);

  let migratedCount = 0;
  let alreadyCloudinaryCount = 0;
  let skippedCount = 0;

  for (let i = 0; i < products.length; i++) {
    const p = products[i];
    if (p.id === "sys_config") {
      skippedCount++;
      continue;
    }

    const currentUrl = p.imageUrls?.[0] || "";
    if (!currentUrl) {
      skippedCount++;
      continue;
    }

    if (currentUrl.includes(`res.cloudinary.com/${CLOUDINARY_CLOUD_NAME}`)) {
      alreadyCloudinaryCount++;
      console.log(`[${i + 1}/${products.length}] ⏩ ALREADY CLOUDINARY: '${p.name}' (${p.id})`);
      continue;
    }

    console.log(`[${i + 1}/${products.length}] ⏳ Uploading to Cloudinary: '${p.name}'...`);
    const cloudinaryUrl = await uploadToCloudinary(currentUrl);

    if (cloudinaryUrl && cloudinaryUrl !== currentUrl && cloudinaryUrl.includes("cloudinary.com")) {
      // Update in AWS AppSync
      const updateMutation = `
        mutation UpdateProductImage($input: UpdateProductInput!) {
          updateProduct(input: $input) {
            id
            name
            imageUrls
          }
        }
      `;

      try {
        const updateRes = await executeGraphQL(updateMutation, {
          input: {
            id: p.id,
            imageUrls: [cloudinaryUrl],
          },
        });

        if (updateRes?.updateProduct?.id) {
          migratedCount++;
          console.log(`  ✅ SAVED TO AWS: ${cloudinaryUrl}`);
        } else {
          console.warn(`  ⚠️ AWS update did not return ID for ${p.id}`);
        }
      } catch (err: any) {
        console.error(`  ❌ AWS update failed for ${p.id}:`, err.message);
      }
    } else {
      console.warn(`  ⚠️ Cloudinary URL not obtained for ${p.id}, keeping existing.`);
    }

    // Small delay to prevent hitting rate limits
    await new Promise((resolve) => setTimeout(resolve, 300));
  }

  console.log("\n=================================================");
  console.log("🎉 Migration Summary:");
  console.log(`✅ Newly Uploaded & Saved in AWS: ${migratedCount}`);
  console.log(`⏩ Already in Cloudinary: ${alreadyCloudinaryCount}`);
  console.log(`⏭️ Skipped (Config/Empty): ${skippedCount}`);
  console.log(`📊 Total Processed: ${products.length}`);
  console.log("=================================================");
}

migrateAllImages().catch(console.error);
