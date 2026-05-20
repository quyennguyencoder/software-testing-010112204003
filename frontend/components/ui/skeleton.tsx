import { cn } from '@/lib/utils';

interface SkeletonProps extends React.HTMLAttributes<HTMLDivElement> {}

/**
 * Skeleton Loading Component
 * Dùng để hiển thị placeholder khi loading dữ liệu
 */
export function Skeleton({
  className,
  ...props
}: SkeletonProps) {
  return (
    <div
      className={cn(
        'animate-pulse rounded-md bg-slate-200',
        className
      )}
      {...props}
    />
  );
}
