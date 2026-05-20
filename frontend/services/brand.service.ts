const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8081/api/v1';

export interface BrandResponse {
  id: number;
  name: string;
  description?: string;
  logoUrl?: string;
  createdAt: string;
  updatedAt: string;
}

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

/**
 * Fetch all brands from backend
 * Uses public endpoint - no authentication required
 */
export async function getAllBrands(): Promise<BrandResponse[]> {
  try {
    const url = `${API_BASE_URL}/brands`;
    console.log('Fetching brands from:', url);

    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      cache: 'no-store',
    });

    console.log('Brands response status:', response.status);

    if (!response.ok) {
      if (response.status === 401 || response.status === 403) {
        console.warn('Not authenticated - returning empty brands');
        return [];
      }
      throw new Error(`Failed to fetch brands: ${response.statusText}`);
    }

    const result: ApiResponse<BrandResponse[]> = await response.json();
    console.log('Brands fetched:', result.data.length);
    return result.data;
  } catch (error) {
    console.error('Error fetching brands:', error);
    return [];
  }
}

/**
 * Fetch single brand by ID
 */
export async function getBrandById(id: number): Promise<BrandResponse | null> {
  try {
    const url = `${API_BASE_URL}/brands/${id}`;
    console.log('Fetching brand from:', url);

    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      cache: 'no-store',
    });

    if (!response.ok) {
      if (response.status === 404) {
        console.warn('Brand not found');
        return null;
      }
      throw new Error(`Failed to fetch brand: ${response.statusText}`);
    }

    const result: ApiResponse<BrandResponse> = await response.json();
    return result.data;
  } catch (error) {
    console.error('Error fetching brand:', error);
    return null;
  }
}
