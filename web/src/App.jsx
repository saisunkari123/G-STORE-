import React from 'react';
import { useStore } from './context/StoreContext';
import Navbar from './components/Navbar';
import CategoryPills from './components/CategoryPills';
import ProductCard from './components/ProductCard';
import ProductDetailModal from './components/ProductDetailModal';
import RadiusCheckerModal from './components/RadiusCheckerModal';
import CartDrawer from './components/CartDrawer';
import OrderTracking from './components/OrderTracking';
import AdminDashboard from './components/AdminDashboard';
import BottomNav from './components/BottomNav';
import { 
  Sparkles, 
  Clock, 
  ShieldCheck, 
  MapPin, 
  Truck, 
  ShoppingBag,
  HeartHandshake
} from 'lucide-react';

export default function App() {
  const { 
    products, 
    selectedCategory, 
    searchQuery, 
    activeTab, 
    currentLocation, 
    setIsRadiusModalOpen 
  } = useStore();

  // Filter products by category & search query
  const filteredProducts = products.filter(p => {
    const matchesCategory = selectedCategory === 'ALL' || 
      p.category.toLowerCase() === selectedCategory.toLowerCase();
    const matchesSearch = searchQuery.trim() === '' || 
      p.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      p.category.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (p.description && p.description.toLowerCase().includes(searchQuery.toLowerCase()));
    return matchesCategory && matchesSearch;
  });

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      {/* Top Navbar */}
      <Navbar />

      {/* Main Content Area */}
      <main className="container" style={{ flex: 1, padding: '0 16px' }}>
        {activeTab === 'SHOP' && (
          <>
            {/* Hero Section Banner */}
            <div
              style={{
                marginTop: '16px',
                borderRadius: 'var(--radius-xl)',
                background: 'linear-gradient(135deg, #00875A 0%, #004D34 100%)',
                color: '#FFFFFF',
                padding: '24px 28px',
                position: 'relative',
                overflow: 'hidden',
                boxShadow: 'var(--shadow-lg)'
              }}
            >
              <div style={{ position: 'relative', zIndex: 2, maxWidth: '640px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
                  <span className="badge badge-gold" style={{ fontSize: '11px' }}>
                    ⚡ RAJAM 10-MIN EXPRESS
                  </span>
                  <span style={{ fontSize: '12px', color: 'rgba(255,255,255,0.85)', fontWeight: '600' }}>
                    Open 6:00 AM - 11:00 PM
                  </span>
                </div>

                <h1 style={{ fontSize: '26px', fontWeight: '800', color: '#FFFFFF', lineHeight: '1.25', marginBottom: '8px' }}>
                  Fresh Staples, Rice & Daily Groceries at Wholesale Rates
                </h1>

                <p style={{ fontSize: '13.5px', color: 'rgba(255,255,255,0.9)', marginBottom: '16px', lineHeight: '1.5' }}>
                  Single-origin Sona Masoori Rice, refined cooking oils, tea-time snacks & cleaning essentials delivered directly from our Rajam hub.
                </p>

                {/* Location Delivery Checker Button */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
                  <button
                    onClick={() => setIsRadiusModalOpen(true)}
                    style={{
                      background: '#FFFFFF',
                      color: '#00875A',
                      fontWeight: '700',
                      fontSize: '13px',
                      padding: '8px 16px',
                      borderRadius: 'var(--radius-full)',
                      display: 'inline-flex',
                      alignItems: 'center',
                      gap: '6px',
                      boxShadow: 'var(--shadow-sm)'
                    }}
                  >
                    <MapPin size={15} />
                    <span>Check Delivery Radius (10 km Zone)</span>
                  </button>

                  <span style={{ fontSize: '12px', color: '#FFDF00', fontWeight: '700' }}>
                    ✓ Free Delivery on orders ₹500+
                  </span>
                </div>
              </div>
            </div>

            {/* Category Pills Bar */}
            <CategoryPills />

            {/* Catalog Products Grid */}
            <section style={{ padding: '16px 0 40px 0' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
                <div>
                  <h2 style={{ fontSize: '18px', fontWeight: '800' }}>
                    {selectedCategory === 'ALL' ? 'All Pantry Products' : selectedCategory}
                  </h2>
                  <p style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                    Showing {filteredProducts.length} items available in Rajam store
                  </p>
                </div>

                {searchQuery && (
                  <span style={{ fontSize: '12.5px', color: 'var(--text-secondary)' }}>
                    Search for: <strong>"{searchQuery}"</strong>
                  </span>
                )}
              </div>

              {filteredProducts.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '60px 20px', background: 'var(--bg-surface)', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border-color)' }}>
                  <ShoppingBag size={40} color="var(--text-muted)" style={{ margin: '0 auto 12px' }} />
                  <h3 style={{ fontSize: '16px', fontWeight: '700', marginBottom: '4px' }}>
                    No products found
                  </h3>
                  <p style={{ fontSize: '12.5px', color: 'var(--text-muted)' }}>
                    Try searching for another keyword or check another category.
                  </p>
                </div>
              ) : (
                <div
                  style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))',
                    gap: '16px'
                  }}
                >
                  {filteredProducts.map((product) => (
                    <ProductCard key={product.id} product={product} />
                  ))}
                </div>
              )}
            </section>
          </>
        )}

        {/* Orders Tracking View */}
        {activeTab === 'ORDERS' && <OrderTracking />}

        {/* Admin Dashboard View */}
        {activeTab === 'ADMIN' && <AdminDashboard />}
      </main>

      {/* Modals & Overlays */}
      <ProductDetailModal />
      <RadiusCheckerModal />
      <CartDrawer />
      <BottomNav />

      {/* Footer */}
      <footer style={{ background: 'var(--bg-surface)', borderTop: '1px solid var(--border-color)', padding: '32px 16px', marginTop: 'auto' }}>
        <div className="container" style={{ display: 'flex', flexDirection: 'column', gap: '16px', textAlign: 'center' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}>
            <span style={{ fontSize: '20px' }}>🛒</span>
            <span style={{ fontFamily: 'var(--font-heading)', fontSize: '18px', fontWeight: '800' }}>G-STORE RAJAM</span>
          </div>

          <p style={{ fontSize: '12px', color: 'var(--text-muted)', maxWidth: '480px', margin: '0 auto' }}>
            Express pantry staples and grocery delivery across Rajam Town, Dollars Colony, GMRIT Campus, Sriharipuram, and surrounding Andhra Pradesh mandals within 10 km.
          </p>

          <div style={{ display: 'flex', justifyContent: 'center', gap: '16px', fontSize: '12px', color: 'var(--text-secondary)' }}>
            <span>📍 Store: Main Road, Rajam, AP (18.4529° N, 83.6548° E)</span>
            <span>📞 Support: +91 98480 12345</span>
          </div>

          <p style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
            © {new Date().getFullYear()} G-Store Inc. All rights reserved.
          </p>
        </div>
      </footer>
    </div>
  );
}
