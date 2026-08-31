import React from 'react';
import { useStore } from '../context/StoreContext';
import { X, Star, ShieldCheck, Truck, RefreshCw, Plus, Minus, Check } from 'lucide-react';

export default function ProductDetailModal() {
  const { selectedProduct, setSelectedProduct, cart, addToCart, removeFromCart } = useStore();

  if (!selectedProduct) return null;

  const quantity = cart[selectedProduct.id] || 0;
  const savings = selectedProduct.mrp - selectedProduct.price;

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        zIndex: 50,
        background: 'rgba(0, 0, 0, 0.65)',
        backdropFilter: 'blur(6px)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '16px'
      }}
      onClick={() => setSelectedProduct(null)}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        style={{
          background: 'var(--bg-surface)',
          borderRadius: 'var(--radius-xl)',
          maxWidth: '680px',
          width: '100%',
          maxHeight: '90vh',
          overflowY: 'auto',
          boxShadow: 'var(--shadow-xl)',
          border: '1px solid var(--border-color)',
          position: 'relative'
        }}
        className="animate-fade"
      >
        {/* Close Button */}
        <button
          onClick={() => setSelectedProduct(null)}
          style={{
            position: 'absolute',
            top: '16px',
            right: '16px',
            width: '36px',
            height: '36px',
            borderRadius: '50%',
            background: 'var(--bg-subtle)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'var(--text-primary)',
            zIndex: 10
          }}
        >
          <X size={18} />
        </button>

        <div style={{ padding: '24px' }}>
          {/* Main Grid */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '24px', alignItems: 'start' }}>
            
            {/* Image */}
            <div
              style={{
                borderRadius: 'var(--radius-lg)',
                overflow: 'hidden',
                background: 'var(--bg-subtle)',
                aspectRatio: '1',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}
            >
              <img
                src={selectedProduct.imageUrl}
                alt={selectedProduct.name}
                style={{ width: '100%', height: '100%', objectFit: 'cover' }}
              />
            </div>

            {/* Details */}
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
                <span className="badge badge-primary">{selectedProduct.category}</span>
                <span className="badge badge-gold">{selectedProduct.unit}</span>
              </div>

              <h2 style={{ fontSize: '20px', fontWeight: '800', marginBottom: '8px', lineHeight: '1.3' }}>
                {selectedProduct.name}
              </h2>

              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '4px', color: '#EAB308' }}>
                  <Star size={16} fill="#EAB308" />
                  <span style={{ fontWeight: '700', fontSize: '14px', color: 'var(--text-primary)' }}>
                    {selectedProduct.rating || 4.8}
                  </span>
                </div>
                <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                  • {selectedProduct.reviews || 95} verified reviews
                </span>
              </div>

              {/* Price Row */}
              <div style={{ background: 'var(--bg-subtle)', padding: '12px 16px', borderRadius: 'var(--radius-md)', marginBottom: '16px' }}>
                <div style={{ display: 'flex', alignItems: 'baseline', gap: '10px' }}>
                  <span style={{ fontSize: '26px', fontWeight: '800', color: 'var(--primary)', fontFamily: 'var(--font-heading)' }}>
                    ₹{selectedProduct.price}
                  </span>
                  {selectedProduct.mrp > selectedProduct.price && (
                    <span style={{ fontSize: '15px', color: 'var(--text-muted)', textDecoration: 'line-through' }}>
                      MRP ₹{selectedProduct.mrp}
                    </span>
                  )}
                  {savings > 0 && (
                    <span className="badge badge-success" style={{ fontSize: '12px' }}>
                      Save ₹{savings}
                    </span>
                  )}
                </div>
                <p style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '4px' }}>
                  Inclusive of all taxes. Free 10-min delivery on orders above ₹500.
                </p>
              </div>

              {/* Description */}
              <div style={{ marginBottom: '20px' }}>
                <h4 style={{ fontSize: '13px', textTransform: 'uppercase', color: 'var(--text-muted)', marginBottom: '6px', letterSpacing: '0.5px' }}>
                  Product Details
                </h4>
                <p style={{ fontSize: '13.5px', color: 'var(--text-secondary)', lineHeight: '1.6' }}>
                  {selectedProduct.description || 'Premium quality pantry essential sourced directly from certified millers and distributors for superior taste and nutrition.'}
                </p>
              </div>

              {/* Trust Badges */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '8px', marginBottom: '24px' }}>
                <div style={{ textAlign: 'center', padding: '8px', background: 'var(--bg-surface)', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-sm)' }}>
                  <ShieldCheck size={18} color="var(--primary)" style={{ margin: '0 auto 4px' }} />
                  <p style={{ fontSize: '11px', fontWeight: '700' }}>100% Genuine</p>
                </div>
                <div style={{ textAlign: 'center', padding: '8px', background: 'var(--bg-surface)', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-sm)' }}>
                  <Truck size={18} color="var(--primary)" style={{ margin: '0 auto 4px' }} />
                  <p style={{ fontSize: '11px', fontWeight: '700' }}>10 Min Delivery</p>
                </div>
                <div style={{ textAlign: 'center', padding: '8px', background: 'var(--bg-surface)', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-sm)' }}>
                  <RefreshCw size={18} color="var(--primary)" style={{ margin: '0 auto 4px' }} />
                  <p style={{ fontSize: '11px', fontWeight: '700' }}>Easy Returns</p>
                </div>
              </div>

              {/* Action Button */}
              {quantity === 0 ? (
                <button
                  onClick={() => addToCart(selectedProduct.id)}
                  disabled={selectedProduct.stock <= 0}
                  className="btn-primary"
                  style={{ width: '100%', padding: '14px', borderRadius: 'var(--radius-md)', fontSize: '15px' }}
                >
                  <Plus size={18} />
                  <span>Add to Cart • ₹{selectedProduct.price}</span>
                </button>
              ) : (
                <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                  <div
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '12px',
                      background: 'var(--primary)',
                      color: '#FFFFFF',
                      borderRadius: 'var(--radius-md)',
                      padding: '8px 16px',
                      boxShadow: 'var(--shadow-emerald)'
                    }}
                  >
                    <button
                      onClick={() => removeFromCart(selectedProduct.id)}
                      style={{ color: '#FFFFFF', display: 'flex', alignItems: 'center' }}
                    >
                      <Minus size={18} />
                    </button>
                    <span style={{ fontSize: '16px', fontWeight: '800', minWidth: '24px', textAlign: 'center' }}>
                      {quantity}
                    </span>
                    <button
                      onClick={() => addToCart(selectedProduct.id)}
                      disabled={quantity >= selectedProduct.stock}
                      style={{ color: '#FFFFFF', display: 'flex', alignItems: 'center' }}
                    >
                      <Plus size={18} />
                    </button>
                  </div>
                  <span style={{ fontSize: '14px', fontWeight: '700', color: 'var(--primary)' }}>
                    ✓ Item added to cart
                  </span>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
