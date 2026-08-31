import React from 'react';
import { useStore } from '../context/StoreContext';
import { 
  ShoppingBag, 
  MapPin, 
  Search, 
  Sun, 
  Moon, 
  ShieldCheck, 
  Store, 
  ClipboardList, 
  CheckCircle2, 
  AlertTriangle,
  X
} from 'lucide-react';

export default function Navbar() {
  const {
    theme,
    toggleTheme,
    currentLocation,
    setIsRadiusModalOpen,
    cartTotalItems,
    setIsCartOpen,
    searchQuery,
    setSearchQuery,
    activeTab,
    setActiveTab
  } = useStore();

  return (
    <header className="sticky top-0 z-40 w-full glass" style={{ borderBottom: '1px solid var(--border-color)' }}>
      <div className="container" style={{ padding: '12px 16px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '16px' }}>
          
          {/* Logo & Brand */}
          <div 
            onClick={() => setActiveTab('SHOP')}
            style={{ display: 'flex', alignItems: 'center', gap: '10px', cursor: 'pointer', userSelect: 'none' }}
          >
            <div 
              style={{ 
                width: '42px', 
                height: '42px', 
                borderRadius: '12px', 
                background: 'var(--primary-gradient)', 
                display: 'flex', 
                alignItems: 'center', 
                justifyContent: 'center',
                boxShadow: 'var(--shadow-emerald)'
              }}
            >
              <span style={{ fontSize: '22px' }}>🛒</span>
            </div>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <span style={{ fontFamily: 'var(--font-heading)', fontSize: '20px', fontWeight: '800', letterSpacing: '-0.5px' }}>
                  G-STORE
                </span>
                <span className="badge badge-gold" style={{ fontSize: '10px', padding: '2px 6px' }}>RAJAM</span>
              </div>
              <p style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: '500' }}>
                10-Min Express Delivery
              </p>
            </div>
          </div>

          {/* Location Delivery Radius Pill */}
          <button
            onClick={() => setIsRadiusModalOpen(true)}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              padding: '6px 12px',
              background: 'var(--bg-subtle)',
              border: `1px solid ${currentLocation.isDeliverable ? 'var(--border-color)' : 'var(--danger)'}`,
              borderRadius: 'var(--radius-full)',
              cursor: 'pointer',
              maxWidth: '320px',
              textAlign: 'left'
            }}
          >
            <MapPin size={16} color={currentLocation.isDeliverable ? 'var(--primary)' : 'var(--danger)'} style={{ flexShrink: 0 }} />
            <div style={{ overflow: 'hidden', whiteSpace: 'nowrap', textOverflow: 'ellipsis' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                <span style={{ fontSize: '12px', fontWeight: '700', color: 'var(--text-primary)' }}>
                  {currentLocation.name.split(',')[0]}
                </span>
                <span style={{ fontSize: '10.5px', color: 'var(--text-muted)' }}>
                  (~{currentLocation.distanceKm} km)
                </span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                {currentLocation.isDeliverable ? (
                  <span style={{ fontSize: '10px', fontWeight: '700', color: 'var(--primary)' }}>
                    ✓ Deliverable zone
                  </span>
                ) : (
                  <span style={{ fontSize: '10px', fontWeight: '700', color: 'var(--danger)' }}>
                    ⚠️ Outside 10km zone
                  </span>
                )}
              </div>
            </div>
          </button>

          {/* Desktop Search Bar */}
          <div 
            style={{ 
              display: 'flex', 
              alignItems: 'center', 
              gap: '8px', 
              flex: '1', 
              maxWidth: '420px',
              background: 'var(--bg-surface)',
              border: '1px solid var(--border-color)',
              borderRadius: 'var(--radius-full)',
              padding: '6px 14px',
              boxShadow: 'var(--shadow-sm)'
            }}
            className="search-input-wrapper"
          >
            <Search size={16} color="var(--text-muted)" />
            <input
              type="text"
              placeholder="Search rice, sunflower oil, snacks, salt..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              style={{
                border: 'none',
                outline: 'none',
                background: 'transparent',
                width: '100%',
                fontSize: '13px',
                color: 'var(--text-primary)'
              }}
            />
            {searchQuery && (
              <button onClick={() => setSearchQuery('')} style={{ color: 'var(--text-muted)', display: 'flex' }}>
                <X size={14} />
              </button>
            )}
          </div>

          {/* Right Action Icons */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            {/* Theme Toggle */}
            <button
              onClick={toggleTheme}
              style={{
                width: '38px',
                height: '38px',
                borderRadius: '10px',
                border: '1px solid var(--border-color)',
                background: 'var(--bg-surface)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'var(--text-secondary)'
              }}
              title="Toggle Dark / Light Mode"
            >
              {theme === 'light' ? <Moon size={18} /> : <Sun size={18} />}
            </button>

            {/* Desktop Navigation Links */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }} className="desktop-nav-links">
              <button
                onClick={() => setActiveTab('SHOP')}
                className={`btn-secondary ${activeTab === 'SHOP' ? 'active-nav' : ''}`}
                style={{ 
                  padding: '8px 12px', 
                  fontSize: '13px',
                  background: activeTab === 'SHOP' ? 'var(--primary-light)' : 'transparent',
                  borderColor: activeTab === 'SHOP' ? 'var(--primary)' : 'transparent',
                  color: activeTab === 'SHOP' ? 'var(--primary)' : 'var(--text-secondary)'
                }}
              >
                <Store size={15} />
                <span>Shop</span>
              </button>

              <button
                onClick={() => setActiveTab('ORDERS')}
                className={`btn-secondary ${activeTab === 'ORDERS' ? 'active-nav' : ''}`}
                style={{ 
                  padding: '8px 12px', 
                  fontSize: '13px',
                  background: activeTab === 'ORDERS' ? 'var(--primary-light)' : 'transparent',
                  borderColor: activeTab === 'ORDERS' ? 'var(--primary)' : 'transparent',
                  color: activeTab === 'ORDERS' ? 'var(--primary)' : 'var(--text-secondary)'
                }}
              >
                <ClipboardList size={15} />
                <span>Orders</span>
              </button>

              <button
                onClick={() => setActiveTab('ADMIN')}
                className={`btn-secondary ${activeTab === 'ADMIN' ? 'active-nav' : ''}`}
                style={{ 
                  padding: '8px 12px', 
                  fontSize: '13px',
                  background: activeTab === 'ADMIN' ? 'var(--secondary-light)' : 'transparent',
                  borderColor: activeTab === 'ADMIN' ? 'var(--secondary)' : 'transparent',
                  color: activeTab === 'ADMIN' ? '#B8860B' : 'var(--text-secondary)'
                }}
              >
                <ShieldCheck size={15} />
                <span>Admin</span>
              </button>
            </div>

            {/* Cart Button */}
            <button
              onClick={() => setIsCartOpen(true)}
              className="btn-primary"
              style={{
                padding: '8px 16px',
                borderRadius: 'var(--radius-full)',
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}
            >
              <ShoppingBag size={18} />
              <span style={{ fontWeight: '700' }}>Cart</span>
              {cartTotalItems > 0 && (
                <span
                  style={{
                    background: '#FFDF00',
                    color: '#000',
                    fontSize: '11px',
                    fontWeight: '800',
                    padding: '2px 7px',
                    borderRadius: '10px'
                  }}
                >
                  {cartTotalItems}
                </span>
              )}
            </button>
          </div>
        </div>
      </div>
    </header>
  );
}
