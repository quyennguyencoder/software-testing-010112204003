/**
 * SearchableSelect - Searchable dropdown component for selecting items
 */
"use client";

import { useState, useEffect, useRef } from "react";
import { Search, ChevronDown, X } from "lucide-react";
import { cn } from "@/lib/utils";

export interface SelectOption {
  value: string;
  label: string;
  description?: string;
}

interface SearchableSelectProps {
  options: SelectOption[];
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  loading?: boolean;
  disabled?: boolean;
  className?: string;
  error?: string;
}

export function SearchableSelect({
  options,
  value,
  onChange,
  placeholder = "Tìm kiếm...",
  loading = false,
  disabled = false,
  className,
  error,
}: SearchableSelectProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const containerRef = useRef<HTMLDivElement>(null);

  // Filter options based on search term
  const filteredOptions = options.filter((option) =>
    option.label.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // Get selected option label
  const selectedOption = options.find((opt) => opt.value === value);
  const displayLabel = selectedOption?.label || placeholder;

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        containerRef.current &&
        !containerRef.current.contains(event.target as Node)
      ) {
        setIsOpen(false);
        setSearchTerm("");
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleSelect = (optionValue: string) => {
    onChange(optionValue);
    setIsOpen(false);
    setSearchTerm("");
  };

  const handleClear = (e: React.MouseEvent) => {
    e.stopPropagation();
    onChange("");
    setSearchTerm("");
  };

  return (
    <div ref={containerRef} className={cn("relative", className)}>
      {/* Trigger Button */}
      <button
        type="button"
        onClick={() => !disabled && setIsOpen(!isOpen)}
        disabled={disabled || loading}
        className={cn(
          "w-full px-3 py-2 text-left border rounded-lg focus:ring-2 focus:ring-blue-500 transition-colors",
          "flex items-center justify-between gap-2",
          "dark:bg-gray-700 dark:text-white dark:border-gray-600",
          disabled || loading
            ? "bg-gray-100 cursor-not-allowed opacity-60"
            : "bg-white hover:bg-gray-50 cursor-pointer",
          error
            ? "border-red-500 focus:ring-red-500"
            : "border-gray-300 dark:border-gray-600"
        )}
      >
        <span
          className={cn(
            "truncate",
            !value && "text-gray-500 dark:text-gray-400"
          )}
        >
          {loading ? "Đang tải..." : displayLabel}
        </span>
        <div className="flex items-center gap-1 flex-shrink-0">
          {value && !disabled && !loading && (
            <X
              className="w-4 h-4 text-gray-400 hover:text-gray-600"
              onClick={handleClear}
            />
          )}
          <ChevronDown
            className={cn(
              "w-4 h-4 text-gray-400 transition-transform",
              isOpen && "transform rotate-180"
            )}
          />
        </div>
      </button>

      {/* Dropdown */}
      {isOpen && !loading && (
        <div className="absolute z-50 w-full mt-1 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-lg shadow-lg max-h-80 overflow-hidden flex flex-col">
          {/* Search Input */}
          <div className="p-2 border-b border-gray-200 dark:border-gray-700">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
              <input
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                placeholder="Tìm kiếm..."
                className="w-full pl-9 pr-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                autoFocus
              />
            </div>
          </div>

          {/* Options List */}
          <div className="overflow-y-auto max-h-64">
            {filteredOptions.length === 0 ? (
              <div className="px-3 py-4 text-center text-gray-500 dark:text-gray-400 text-sm">
                Không tìm thấy kết quả
              </div>
            ) : (
              filteredOptions.map((option) => (
                <button
                  key={option.value}
                  type="button"
                  onClick={() => handleSelect(option.value)}
                  className={cn(
                    "w-full px-3 py-2 text-left hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors",
                    option.value === value &&
                      "bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400"
                  )}
                >
                  <div className="font-medium text-sm">{option.label}</div>
                  {option.description && (
                    <div className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">
                      {option.description}
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
