'use client';

import { useState } from 'react';
import { AddressForm } from '@/components/common/AddressForm';
import { AddressFormData } from '@/types/address';

/**
 * Demo page for AddressForm component
 * Shows how to use the cascading address dropdown
 */
export default function AddressFormDemo() {
  const [selectedAddress, setSelectedAddress] = useState<AddressFormData | null>(null);

  const handleAddressChange = (address: AddressFormData) => {
    setSelectedAddress(address);
    console.log('Address changed:', address);
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-2xl mx-auto">
        <div className="bg-white rounded-lg shadow-md p-6">
          <h1 className="text-3xl font-bold mb-2">Vietnamese Address Form Demo</h1>
          <p className="text-gray-600 mb-6">
            Demo form for selecting Vietnamese provinces, districts, and wards with cascading dropdowns
          </p>

          <div className="mb-8">
            <AddressForm onAddressChange={handleAddressChange} showStreetAddress={true} />
          </div>

          {selectedAddress && (
            <div className="mt-8 bg-green-50 border border-green-200 rounded-lg p-4">
              <h2 className="text-lg font-semibold text-green-900 mb-4">Selected Address Data:</h2>
              <pre className="bg-white p-4 rounded border border-green-100 overflow-auto text-sm">
                {JSON.stringify(selectedAddress, null, 2)}
              </pre>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
