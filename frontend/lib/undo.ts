import { toast } from 'sonner';

type RestoreFn = () => void | Promise<void>;
type FinalizeFn = () => Promise<void>;

interface PendingEntry {
  itemId: number;
  timer: ReturnType<typeof setTimeout>;
  restore: RestoreFn;
}

const pending = new Map<number, PendingEntry>();

export function scheduleDelete(itemId: number, finalize: FinalizeFn, restore: RestoreFn, timeout = 5000) {
  // If already pending, ignore
  if (pending.has(itemId)) return;

  const timer = setTimeout(async () => {
    try {
      await finalize();
    } catch (e: any) {
      // finalize failed: attempt restore and notify
      try {
        await restore();
      } catch {}
      toast.error((e && e.message) || 'Lỗi khi xóa sản phẩm');
    } finally {
      pending.delete(itemId);
    }
  }, timeout);

  pending.set(itemId, { itemId, timer, restore });
}

export function undoDelete(itemId: number, suppressToast: boolean = false) {
  const entry = pending.get(itemId);
  if (!entry) return false;
  clearTimeout(entry.timer);
  pending.delete(itemId);
  try {
    entry.restore();
  } catch (e) {
    console.error('Failed to restore item on undo:', e);
  }
  if (!suppressToast) {
    toast.success('Hoàn tác thành công');
  }
  return true;
}

export function undoMultiple(itemIds: number[]) {
  let undone = 0;
  for (const id of itemIds) {
    const ok = undoDelete(id, true); // suppress individual toasts
    if (ok) undone += 1;
  }
  if (undone > 0) {
    toast.success(`Hoàn tác ${undone} mục thành công`);
    return true;
  }
  return false;
}

export function hasPending(itemId: number) {
  return pending.has(itemId);
}
