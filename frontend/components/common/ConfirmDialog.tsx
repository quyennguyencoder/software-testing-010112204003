"use client";

import { Check, AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';

interface ConfirmDialogProps {
  open: boolean;
  title?: string;
  description?: string;
  confirmLabel?: string;
  cancelLabel?: string;
  intent?: 'danger' | 'primary';
  onConfirm: () => void;
  onClose: () => void;
}

export default function ConfirmDialog({
  open,
  title = 'Xác nhận',
  description,
  confirmLabel = 'Xác nhận',
  cancelLabel = 'Hủy',
  intent = 'danger',
  onConfirm,
  onClose,
}: ConfirmDialogProps) {
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50">
      <div className="fixed inset-0 bg-black/40" onClick={onClose}></div>
      <div className="fixed inset-0 flex items-center justify-center p-4">
        <div className="bg-white rounded-lg shadow-xl max-w-lg w-full">
          <div className="flex items-center gap-3 p-6 border-b">
            <div className="p-2 rounded-full bg-red-50 text-red-600">
              {intent === 'danger' ? <AlertCircle className="h-6 w-6" /> : <Check className="h-6 w-6" />}
            </div>
            <div>
              <h3 className="text-lg font-semibold">{title}</h3>
              {description && <p className="text-sm text-gray-600 mt-1">{description}</p>}
            </div>
          </div>

          <div className="p-4 flex justify-end gap-3">
            <Button variant="outline" onClick={onClose} className="bg-white">
              {cancelLabel}
            </Button>
            <Button
              onClick={() => {
                onConfirm();
              }}
              className={intent === 'danger' ? 'bg-red-600 text-white hover:bg-red-700' : ''}
            >
              {confirmLabel}
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
