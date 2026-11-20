import type { ButtonHTMLAttributes, ReactNode } from 'react';

export function Avatar({ label, tone, online = false, size = 'medium' }: { label: string; tone: string; online?: boolean; size?: 'small' | 'medium' | 'large' }) {
  return <span className={`avatar avatar-${tone} avatar-${size}`} aria-label={label}>{label}<span className={online ? 'presence-dot' : 'presence-dot hidden'} /></span>;
}

export function IconButton({ label, children, className = '', ...props }: ButtonHTMLAttributes<HTMLButtonElement> & { label: string; children: ReactNode }) {
  return <button type="button" aria-label={label} title={label} className={`icon-button ${className}`} {...props}>{children}</button>;
}
