package com.example.data.repository

import com.example.domain.model.Product
import com.example.domain.model.ProductVariant

val initialProducts = listOf(
    Product(
        id = "p_akshaya",
        categoryId = "c_rice",
        nameEn = "Akshaya Rice",
        nameTe = "అక్షయ బియ్యం",
        brand = "Akshaya",
        descriptionEn = "Fresh quality rice delivered directly from the mill. Ideal for everyday home cooking.",
        descriptionTe = "మిల్లు నుండి నేరుగా అందించే తాజా నాణ్యమైన బియ్యం. రోజువారీ వంట కోసం అనువైనది.",
        shortDescriptionEn = "Quality everyday rice from Akshaya brand.",
        shortDescriptionTe = "అక్షయ బ్రాండ్ నాణ్యమైన బియ్యం.",
        imageUrls = listOf("android.resource://com.aistudio.ricemart.pkqmsx/drawable/akshaya_rice_bag"),
        variants = listOf(
            ProductVariant(id = "ak_5",  weight = "5",  unit = "Kg", currentPrice = 350.0, mrp = 400.0, stockQuantity = 25, sku = "AK-5KG"),
            ProductVariant(id = "ak_10", weight = "10",  unit = "Kg", currentPrice = 700.0, mrp = 800.0, stockQuantity = 5, sku = "AK-10KG"),
            ProductVariant(id = "ak_26", weight = "26",  unit = "Kg", currentPrice = 1750.0, mrp = 2000.0, stockQuantity = 0, sku = "AK-26KG")
        )
    ),
    Product(
        id = "p_sameera",
        categoryId = "c_rice",
        nameEn = "Sameera Rice",
        nameTe = "సమీరా బియ్యం",
        brand = "Sameera",
        descriptionEn = "Trusted Sameera brand rice — soft texture, consistent quality for the whole family.",
        descriptionTe = "నమ్మదగిన సమీరా బ్రాండ్ బియ్యం — మెత్తటి రుచి, మొత్తం కుటుంబానికి నాణ్యత.",
        shortDescriptionEn = "Soft and consistent Sameera rice.",
        shortDescriptionTe = "సమీరా మెత్తటి నాణ్యమైన బియ్యం.",
        imageUrls = listOf("android.resource://com.aistudio.ricemart.pkqmsx/drawable/sameera_rice_bag"),
        variants = listOf(
            ProductVariant(id = "sm_5",  weight = "5",  unit = "Kg", currentPrice = 350.0, mrp = 400.0, stockQuantity = 0, sku = "SM-5KG"),
            ProductVariant(id = "sm_10", weight = "10",  unit = "Kg", currentPrice = 700.0, mrp = 800.0, stockQuantity = 15, sku = "SM-10KG"),
            ProductVariant(id = "sm_26", weight = "26",  unit = "Kg", currentPrice = 1750.0, mrp = 2000.0, stockQuantity = 3, sku = "SM-26KG")
        )
    ),
    Product(
        id = "p_bell",
        categoryId = "c_rice",
        nameEn = "Bell Brand Rice",
        nameTe = "బెల్ బ్రాండ్ బియ్యం",
        brand = "Bell Brand",
        descriptionEn = "Bell Brand — popular choice for clean, fluffy grains. Great for rice dishes.",
        descriptionTe = "బెల్ బ్రాండ్ — శుభ్రమైన, మృదువైన గింజల కోసం ప్రసిద్ధ ఎంపిక.",
        shortDescriptionEn = "Fluffy clean grains from Bell Brand.",
        shortDescriptionTe = "బెల్ బ్రాండ్ మెత్తటి శుభ్రమైన బియ్యం.",
        imageUrls = listOf("android.resource://com.aistudio.ricemart.pkqmsx/drawable/bell_brand_rice_bag"),
        variants = listOf(
            ProductVariant(id = "bb_5",  weight = "5",  unit = "Kg", currentPrice = 350.0, mrp = 400.0, stockQuantity = 8, sku = "BB-5KG"),
            ProductVariant(id = "bb_10", weight = "10",  unit = "Kg", currentPrice = 700.0, mrp = 800.0, stockQuantity = 0, sku = "BB-10KG"),
            ProductVariant(id = "bb_26", weight = "26",  unit = "Kg", currentPrice = 1750.0, mrp = 2000.0, stockQuantity = 50, sku = "BB-26KG")
        )
    ),
    Product(
        id = "p_lalitha",
        categoryId = "c_rice",
        nameEn = "Lalitha Brand Rice",
        nameTe = "లలిత బ్రాండ్ బియ్యం",
        brand = "Lalitha Brand",
        descriptionEn = "Lalitha Brand rice — fine quality, light on stomach, perfect for everyday use.",
        descriptionTe = "లలిత బ్రాండ్ బియ్యం — మంచి నాణ్యత, జీర్ణానికి తేలికగా ఉండే రోజువారీ బియ్యం.",
        shortDescriptionEn = "Light and quality Lalitha Brand rice.",
        shortDescriptionTe = "లలిత బ్రాండ్ తేలికైన నాణ్యమైన బియ్యం.",
        imageUrls = listOf("android.resource://com.aistudio.ricemart.pkqmsx/drawable/lalitha_brand_rice_bag"),
        variants = listOf(
            ProductVariant(id = "lb_5",  weight = "5",  unit = "Kg", currentPrice = 350.0, mrp = 400.0, stockQuantity = 30, sku = "LB-5KG"),
            ProductVariant(id = "lb_10", weight = "10",  unit = "Kg", currentPrice = 700.0, mrp = 800.0, stockQuantity = 2, sku = "LB-10KG"),
            ProductVariant(id = "lb_26", weight = "26",  unit = "Kg", currentPrice = 1750.0, mrp = 2000.0, stockQuantity = 0, sku = "LB-26KG")
        )
    )
)
