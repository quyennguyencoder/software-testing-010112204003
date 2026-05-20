import * as React from 'react'

import { cn } from '@/lib/utils'

interface BadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
  variant?: 'default' | 'secondary' | 'destructive' | 'outline'
}

export function Badge({ className, variant = 'default', ...props }: BadgeProps) {
  const base = 'inline-flex items-center px-2 py-0.5 rounded-md text-sm font-medium'
  const variants: Record<string, string> = {
    default: 'bg-muted text-muted-foreground',
    secondary: 'bg-gray-100 text-gray-800',
    destructive: 'bg-red-600 text-white',
    outline: 'border border-gray-300 bg-white text-gray-700',
  }

  return <span className={cn(base, variants[variant] ?? variants.default, className)} {...props} />
}

export default Badge
