import React from 'react';
import { useStore } from '../context/StoreContext';
import { Plus, Minus, Star } from 'lucide-react';

export default function ProductCard({ product }) {
  const { cart, addToCart, removeFromCart, setSelectedProduct } = useStore();
  const quantity = cart[product.id] || 0;

  const discountPercent = product.mrp > product.price 
    ? Math.round(((product.mrp - product.price) / product.mrp) * 100) 
    : 0;

  return (
    <div
      style={{
        background: 'var(--bg-surface)',
        border: '1px solid var(--border-color)',
        borderRadius: 'var(--radius-lg)',
        padding: '14px',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between',
        position: 'relative',
        transition: 'var(--transition)',
        boxShadow: 'var(--shadow-sm)'
      }}
      className="product-card"
    >
      {/* Discount Badge */}
      {discountPercent > 0 && (
        <div 
          style={{ 
            position: 'absolute', 
            top: '12px', 
            left: '12px', 
            zIndex: 10,
            background: 'var(--primary-gradient)',
            color: '#FFFFFF',
            fontSize: '11px',
            fontWeight: '800',
            padding: '3px 8px',
            borderRadius: 'var(--radius-sm)',
            boxShadow: 'var(--shadow-sm)'
          }}
        >
          {discountPercent}% OFF
        </div>
      )}

      {/* Product Image */}
      <div
        onClick={() => setSelectedProduct(product)}
        style={{
          width: '100%',
          height: '160px',
          borderRadius: 'var(--radius-md)',
          overflow: 'hidden',
          background: 'var(--bg-subtle)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          cursor: 'pointer',
          marginBottom: '12px'
        }}
      >
        <img
          src={product.imageUrl}
          alt={product.name}
          style={{
            width: '100%',
            height: '100%',
            objectFit: 'cover',
            transition: 'transform 0.3s ease'
          }}
          className="product-img-hover"
        />
      </div>

      {/* Content */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
        {/* Category & Unit */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '4px' }}>
          <span style={{ fontSize: '11px', fontWeight: '700', color: 'var(--primary)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
            {product.category}
          </span>
          <span className="badge badge-gold" style={{ fontSize: '10px', padding: '1px 6px' }}>
            {product.unit}
          </span>
        </div>

        {/* Title */}
        <h3
          onClick={() => setSelectedProduct(product)}
          style={{
            fontSize: '14px',
            fontWeight: '700',
            color: 'var(--text-primary)',
            lineHeight: '1.3',
            cursor: 'pointer',
            marginBottom: '6px',
            display: '-webkit-box',
            WebkitLineClamp: 2,
            WebkitBoxOrient: 'vertical',
            overflow: 'hidden'
          }}
          title={product.name}
        >
          {product.name}
        </h3>

        {/* Rating */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '4px', marginBottom: '10px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '2px', color: '#EAB308' }}>
            <Star size={13} fill="#EAB308" />
            <span style={{ fontSize: '12px', fontWeight: '700', color: 'var(--text-primary)' }}>
              {product.rating || 4.8}
            </span>
          </div>
          <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
            ({product.reviews || 42})
          </span>
          <span style={{ fontSize: '11px', color: 'var(--text-muted)', marginLeft: 'auto' }}>
            Stock: <strong style={{ color: product.stock > 0 ? 'var(--primary)' : 'var(--danger)' }}>{product.stock}</strong>
          </span>
        </div>

        {/* Price & Action Row */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: 'auto', paddingTop: '8px', borderTop: '1px dashed var(--border-color)' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'baseline', gap: '6px' }}>
              <span style={{ fontSize: '18px', fontWeight: '800', color: 'var(--text-primary)', fontFamily: 'var(--font-heading)' }}>
                ₹{product.price}
              </span>
              {product.mrp > product.price && (
                <span style={{ fontSize: '12px', color: 'var(--text-muted)', textDecoration: 'line-through' }}>
                  ₹{product.mrp}
                </span>
              )}
            </div>
            <p style={{ fontSize: '10.5px', color: 'var(--primary)', fontWeight: '600' }}>
              Save ₹{product.mrp - product.price}
            </p>
          </div>

          {/* Add / Stepper Button */}
          {quantity === 0 ? (
            <button
              onClick={() => addToCart(product.id)}
              disabled={product.stock <= 0}
              className="btn-primary"
              style={{
                padding: '6px 14px',
                fontSize: '12.5px',
                borderRadius: 'var(--radius-full)',
                fontWeight: '700'
              }}
            >
              <Plus size={14} />
              <span>ADD</span>
            </button>
          ) : (
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                background: 'var(--primary)',
                color: '#FFFFFF',
                borderRadius: 'var(--radius-full)',
                padding: '4px 8px',
                boxShadow: 'var(--shadow-emerald)'
              }}
            >
              <button
                onClick={() => removeFromCart(product.id)}
                style={{
                  width: '24px',
                  height: '24px',
                  borderRadius: '50%',
                  background: 'rgba(255, 255, 255, 0.25)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: '#FFFFFF'
                }}
              >
                <Minus size={13} />
              </button>
              <span style={{ fontSize: '13px', fontWeight: '800', minWidth: '16px', textAlign: 'center' }}>
                {quantity}
              </span>
              <button
                onClick={() => addToCart(product.id)}
                disabled={quantity >= product.stock}
                style={{
                  width: '24px',
                  height: '24px',
                  borderRadius: '50%',
                  background: 'rgba(255, 255, 255, 0.25)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: '#FFFFFF'
                }}
              >
                <Plus size={13} />
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
