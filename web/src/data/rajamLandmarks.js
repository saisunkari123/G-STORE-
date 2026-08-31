export const STORE_HUB = {
  name: 'G-Store Rajam Central Hub',
  lat: 18.4529,
  lng: 83.6548,
  deliveryRadiusKm: 10.0
};

export function calculateDistanceKm(lat1, lon1, lat2, lon2) {
  const R = 6371; // Radius of earth in km
  const dLat = (lat2 - lat1) * (Math.PI / 180);
  const dLon = (lon2 - lon1) * (Math.PI / 180);
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * (Math.PI / 180)) *
      Math.cos(lat2 * (Math.PI / 180)) *
      Math.sin(dLon / 2) *
      Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return Number((R * c).toFixed(1));
}

// Live Dynamic OpenStreetMap Geocoder (Worldwide & India places search)
export async function searchLivePlaces(query) {
  if (!query || query.trim().length < 2) return [];
  const qTrim = query.trim();
  const results = [];

  try {
    const encoded = encodeURIComponent(qTrim);
    const res = await fetch(
      `https://nominatim.openstreetmap.org/search?format=json&q=${encoded}&countrycodes=in&limit=8`
    );
    if (res.ok) {
      const data = await res.json();
      data.forEach((item) => {
        const lat = parseFloat(item.lat);
        const lng = parseFloat(item.lon);
        const dist = calculateDistanceKm(STORE_HUB.lat, STORE_HUB.lng, lat, lng);
        results.push({
          name: item.display_name.split(',')[0],
          area: item.display_name,
          lat,
          lng,
          distanceKm: dist,
          isDeliverable: dist <= STORE_HUB.deliveryRadiusKm
        });
      });
    }
  } catch (err) {
    console.error('Dynamic geocode error:', err);
  }

  return results;
}
