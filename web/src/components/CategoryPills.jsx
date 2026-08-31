import React from 'react';
import { useStore } from '../context/StoreContext';

export default function CategoryPills() {
  const { categories, products, selectedCategory, setSelectedCategory } = useStore();

  const allCount = products.length;

  return (
    <div style={{ padding: '16px 0 8px 0', overflowX: 'auto', WebkitOverflowScrolling: 'touch' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', paddingBottom: '4px' }}>
        {/* All Products Pill */}
        <button
          onClick={() => setSelectedCategory('ALL')}
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '8px',
            padding: '8px 16px',
            borderRadius: 'var(--radius-full)',
            fontSize: '13px',
            fontWeight: '600',
            whiteSpace: 'nowrap',
            cursor: 'pointer',
            transition: 'var(--transition)',
            background: selectedCategory === 'ALL' ? 'var(--primary-gradient)' : 'var(--bg-surface)',
            color: selectedCategory === 'ALL' ? '#FFFFFF' : 'var(--text-secondary)',
            border: selectedCategory === 'ALL' ? '1px solid transparent' : '1px solid var(--border-color)',
            boxShadow: selectedCategory === 'ALL' ? 'var(--shadow-emerald)' : 'var(--shadow-sm)'
          }}
        >
          <span>🛍️</span>
          <span>All Items</span>
          <span 
            style={{ 
              fontSize: '11px',
              padding: '1px 6px',
              borderRadius: '8px',
              background: selectedCategory === 'ALL' ? 'rgba(255,255,255,0.25)' : 'var(--bg-subtle)',
              color: selectedCategory === 'ALL' ? '#FFF' : 'var(--text-muted)'
            }}
          >
            {allCount}
          </span>
        </button>

        {/* Dynamic 5 Categories */}
        {categories.map((cat) => {
          const isSelected = selectedCategory.toLowerCase() === cat.name.toLowerCase();
          const categoryProductCount = products.filter(
            p => p.category.toLowerCase() === cat.name.toLowerCase()
          ).length;

          return (
            <button
              key={cat.id || cat.name}
              onClick={() => setSelectedCategory(cat.name)}
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '8px',
                padding: '8px 16px',
                borderRadius: 'var(--radius-full)',
                fontSize: '13px',
                fontWeight: '600',
                whiteSpace: 'nowrap',
                cursor: 'pointer',
                transition: 'var(--transition)',
                background: isSelected ? 'var(--primary-gradient)' : 'var(--bg-surface)',
                color: isSelected ? '#FFFFFF' : 'var(--text-secondary)',
                border: isSelected ? '1px solid transparent' : '1px solid var(--border-color)',
                boxShadow: isSelected ? 'var(--shadow-emerald)' : 'var(--shadow-sm)'
              }}
            >
              <span>{cat.icon || '📦'}</span>
              <span>{cat.name}</span>
              <span 
                style={{ 
                  fontSize: '11px',
                  padding: '1px 6px',
                  borderRadius: '8px',
                  background: isSelected ? 'rgba(255,255,255,0.25)' : 'var(--bg-subtle)',
                  color: isSelected ? '#FFF' : 'var(--text-muted)'
                }}
              >
                {categoryProductCount}
              </span>
            </button>
          );
        })}
      </div>
    </div>
  );
}
