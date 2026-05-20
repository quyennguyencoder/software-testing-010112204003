/**
 * Custom hook for form validation
 * Provides validation utilities for forms
 */

'use client';

import { useState, useCallback } from 'react';
import {
  validateEmail,
  validatePassword,
  validateUsername,
  validatePhoneNumber,
  validateRequired,
  validatePasswordMatch,
  type ValidationResult,
} from '@/lib/utils/validators';

interface FormErrors {
  [key: string]: string;
}

export function useFormValidation() {
  const [errors, setErrors] = useState<FormErrors>({});

  const clearError = useCallback((field: string) => {
    setErrors((prev) => {
      const newErrors = { ...prev };
      delete newErrors[field];
      return newErrors;
    });
  }, []);

  const clearAllErrors = useCallback(() => {
    setErrors({});
  }, []);

  const setError = useCallback((field: string, message: string) => {
    setErrors((prev) => ({ ...prev, [field]: message }));
  }, []);

  const validate = useCallback((
    field: string,
    value: string,
    type: 'email' | 'password' | 'username' | 'phone' | 'required',
    options?: { fieldName?: string; compareValue?: string }
  ): boolean => {
    let result: ValidationResult;

    switch (type) {
      case 'email':
        result = validateEmail(value);
        break;
      case 'password':
        result = validatePassword(value);
        break;
      case 'username':
        result = validateUsername(value);
        break;
      case 'phone':
        result = validatePhoneNumber(value);
        break;
      case 'required':
        result = validateRequired(value, options?.fieldName || field);
        break;
      default:
        result = { isValid: true };
    }

    if (!result.isValid && result.error) {
      setError(field, result.error);
      return false;
    }

    clearError(field);
    return true;
  }, [clearError, setError]);

  const validatePasswordConfirmation = useCallback((
    password: string,
    confirmPassword: string
  ): boolean => {
    const result = validatePasswordMatch(password, confirmPassword);
    
    if (!result.isValid && result.error) {
      setError('confirmPassword', result.error);
      return false;
    }

    clearError('confirmPassword');
    return true;
  }, [clearError, setError]);

  return {
    errors,
    validate,
    validatePasswordConfirmation,
    setError,
    clearError,
    clearAllErrors,
  };
}
