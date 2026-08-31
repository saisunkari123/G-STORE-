import React from 'react';
import { useStore } from '../context/StoreContext';
import { Gift, CheckCircle, Sparkles } from 'lucide-react';
import { MILESTONE_GIFTS } from '../data/initialProducts';

export default function MilestoneGifts() {
  const { cartSubtotal, selectedGiftId, setSelectedGiftId, unlockedGifts, nextGift } = useStore();

  const maxThreshold = 1499;
  const progressPercent = Math.min(100, Math.round((cartSubtotal / maxThreshold) * 100));

  return (
    <div
      style={{
        background: 'linear-gradient(135deg, rgba(212, 175, 55, 0.1) 0%, rgba(0, 135, 90, 0.08) 100%)',
        border: '1px solid var(--secondary)',
        borderRadius: 'var(--radius-lg)',
        padding: '14px 16px',
        margin: '12px 0'
      }}
    >
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '8px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <div
            style={{
              width: '28px',
              height: '28px',
              borderRadius: '50%',
              background: 'var(--gold-gradient)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#000'
            }}
          >
            <Gift size={15} />
          </div>
          <div>
            <span style={{ fontSize: '13px', fontWeight: '800', color: 'var(--text-primary)' }}>
              Tiered Milestone Rewards
            </span>
            <p style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
              Unlock free gifts with higher cart value
            </p>
          </div>
        </div>
        <span className="badge badge-gold" style={{ fontSize: '11px' }}>
          {unlockedGifts.length} / {MILESTONE_GIFTS.length} Unlocked
        </span>
      </div>

      {/* Progress Bar */}
      <div style={{ margin: '10px 0' }}>
        <div
          style={{
            height: '8px',
            background: 'var(--border-color)',
            borderRadius: '4px',
            overflow: 'hidden',
            position: 'relative'
          }}
        >
          <div
            style={{
              height: '100%',
              width: `${progressPercent}%`,
              background: 'var(--gold-gradient)',
              borderRadius: '4px',
              transition: 'width 0.4s ease'
            }}
          />
        </div>
      </div>

      {/* Next Tier FOMO message */}
      {nextGift ? (
        <p style={{ fontSize: '12px', fontWeight: '600', color: 'var(--text-primary)', display: 'flex', alignItems: 'center', gap: '4px' }}>
          <Sparkles size={14} color="#D4AF37" />
          <span>Add <strong>₹{nextGift.threshold - cartSubtotal}</strong> more to unlock <strong>{nextGift.title}</strong>!</span>
        </p>
      ) : (
        <p style={{ fontSize: '12px', fontWeight: '700', color: 'var(--primary)', display: 'flex', alignItems: 'center', gap: '4px' }}>
          <CheckCircle size={14} color="var(--primary)" />
          <span>🎉 Congratulations! All milestone gifts unlocked!</span>
        </p>
      )}

      {/* Gift Selection List */}
      {unlockedGifts.length > 0 && (
        <div style={{ marginTop: '12px', paddingTop: '10px', borderTop: '1px dashed var(--border-color)' }}>
          <p style={{ fontSize: '11.5px', fontWeight: '700', color: 'var(--text-secondary)', marginBottom: '8px' }}>
            SELECT YOUR FREE GIFT:
          </p>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            {unlockedGifts.map((gift) => {
              const isSelected = selectedGiftId === gift.id;
              return (
                <div
                  key={gift.id}
                  onClick={() => setSelectedGiftId(isSelected ? null : gift.id)}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    padding: '8px 12px',
                    borderRadius: 'var(--radius-sm)',
                    background: isSelected ? 'var(--primary-light)' : 'var(--bg-surface)',
                    border: `1px solid ${isSelected ? 'var(--primary)' : 'var(--border-color)'}`,
                    cursor: 'pointer',
                    transition: 'var(--transition)'
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <input
                      type="radio"
                      checked={isSelected}
                      onChange={() => {}}
                      style={{ accentColor: 'var(--primary)' }}
                    />
                    <img
                      src={gift.imageUrl}
                      alt={gift.title}
                      style={{ width: '32px', height: '32px', borderRadius: '6px', objectFit: 'cover' }}
                    />
                    <span style={{ fontSize: '12px', fontWeight: '600', color: 'var(--text-primary)' }}>
                      {gift.title}
                    </span>
                  </div>
                  <span className="badge badge-success" style={{ fontSize: '10.5px' }}>
                    FREE (Worth ₹{gift.actualValue})
                  </span>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}
