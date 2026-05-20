/**
 * Combobox component - Input field với dropdown suggestions
 * Cho phép người dùng tự nhập HOẶC chọn từ dropdown
 */

'use client';

import { useState, useEffect, useRef, useCallback } from 'react';
import { ChevronDown, X, Loader2 } from 'lucide-react';
import { cn } from '@/lib/utils';

export interface ComboboxOption {
  value: string;
  label: string;
  code?: string | number;
}

interface ComboboxProps {
  options: ComboboxOption[];
  value: string;
  onChange: (value: string) => void;
  onSelect?: (option: ComboboxOption) => void; // Callback khi chọn từ dropdown
  placeholder?: string;
  loading?: boolean;
  disabled?: boolean;
  className?: string;
  error?: string;
  allowCustomInput?: boolean; // Cho phép tự nhập (default: true)
  filterOptions?: (options: ComboboxOption[], searchTerm: string) => ComboboxOption[];
}

export function Combobox({
  options,
  value,
  onChange,
  onSelect,
  placeholder = 'Nhập hoặc chọn...',
  loading = false,
  disabled = false,
  className,
  error,
  allowCustomInput = true,
  filterOptions,
}: ComboboxProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [inputValue, setInputValue] = useState(value);
  const [searchTerm, setSearchTerm] = useState('');
  const containerRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  // Sync inputValue với value prop
  useEffect(() => {
    setInputValue(value);
  }, [value]);

  // Filter options
  const getFilteredOptions = useCallback(() => {
    if (!searchTerm.trim()) return options;
    
    if (filterOptions) {
      return filterOptions(options, searchTerm);
    }
    
    // Default filter: tìm kiếm trong label
    return options.filter((option) =>
      option.label.toLowerCase().includes(searchTerm.toLowerCase())
    );
  }, [options, searchTerm, filterOptions]);

  const filteredOptions = getFilteredOptions();

  // Get selected option label (nếu value match với option)
  const selectedOption = options.find((opt) => opt.value === value || opt.label === value);
  const displayValue = selectedOption?.label || inputValue;

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        containerRef.current &&
        !containerRef.current.contains(event.target as Node)
      ) {
        setIsOpen(false);
        setSearchTerm('');
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Handle input change
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newValue = e.target.value;
    setInputValue(newValue);
    setSearchTerm(newValue);
    onChange(newValue);
    
    // Mở dropdown khi bắt đầu nhập
    if (newValue && !isOpen) {
      setIsOpen(true);
    }
  };

  // Handle input focus
  const handleInputFocus = () => {
    if (!disabled && !loading) {
      setIsOpen(true);
      setSearchTerm(inputValue);
    }
  };

  // Handle select option
  const handleSelectOption = (option: ComboboxOption) => {
    setInputValue(option.label);
    setSearchTerm('');
    onChange(option.label);
    if (onSelect) {
      onSelect(option);
    }
    setIsOpen(false);
    inputRef.current?.blur();
  };

  // Handle clear
  const handleClear = (e: React.MouseEvent) => {
    e.stopPropagation();
    setInputValue('');
    setSearchTerm('');
    onChange('');
    setIsOpen(false);
  };

  // Handle input blur (delay để cho phép click vào option)
  const handleInputBlur = () => {
    setTimeout(() => {
      setIsOpen(false);
    }, 200);
  };

  return (
    <div ref={containerRef} className={cn('relative', className)}>
      {/* Input Field */}
      <div className="relative">
        <input
          ref={inputRef}
          type="text"
          value={inputValue}
          onChange={handleInputChange}
          onFocus={handleInputFocus}
          onBlur={handleInputBlur}
          placeholder={loading ? 'Đang tải...' : placeholder}
          disabled={disabled || loading}
          className={cn(
            'w-full px-3 py-2 pr-10 border rounded-lg',
            'focus:ring-2 focus:ring-blue-500 focus:border-blue-500',
            'disabled:opacity-50 disabled:cursor-not-allowed',
            'bg-white dark:bg-gray-800',
            error
              ? 'border-red-500 focus:ring-red-500'
              : 'border-gray-300 dark:border-gray-600',
            className
          )}
        />
        <div className="absolute right-2 top-1/2 -translate-y-1/2 flex items-center gap-1">
          {inputValue && !disabled && !loading && (
            <button
              type="button"
              onClick={handleClear}
              className="p-1 hover:bg-gray-100 rounded transition-colors"
            >
              <X className="w-4 h-4 text-gray-400" />
            </button>
          )}
          {loading ? (
            <Loader2 className="w-4 h-4 animate-spin text-gray-400" />
          ) : (
            <ChevronDown
              className={cn(
                'w-4 h-4 text-gray-400 transition-transform',
                isOpen && 'transform rotate-180'
              )}
            />
          )}
        </div>
      </div>

      {/* Dropdown */}
      {isOpen && !loading && (
        <div className="absolute z-50 w-full mt-1 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-lg shadow-lg max-h-80 overflow-hidden flex flex-col">
          {/* Options List */}
          <div className="overflow-y-auto max-h-64">
            {filteredOptions.length === 0 ? (
              <div className="px-3 py-4 text-center text-gray-500 dark:text-gray-400 text-sm">
                {searchTerm ? 'Không tìm thấy kết quả' : 'Không có dữ liệu'}
              </div>
            ) : (
              filteredOptions.map((option) => (
                <button
                  key={option.value}
                  type="button"
                  onClick={() => handleSelectOption(option)}
                  className={cn(
                    'w-full px-3 py-2 text-left hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors',
                    'border-b border-gray-100 dark:border-gray-700 last:border-0',
                    (option.value === value || option.label === value) &&
                      'bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400'
                  )}
                >
                  <div className="font-medium text-sm">{option.label}</div>
                  {option.code && (
                    <div className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">
                      Mã: {option.code}
                    </div>
                  )}
                </button>
              ))
            )}
          </div>
        </div>
      )}

      {/* Error Message */}
      {error && <p className="text-xs text-red-500 mt-1">{error}</p>}
    </div>
  );
}

