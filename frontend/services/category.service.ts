const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8081/api/v1';

export interface CategoryResponse {
  id: number;
  name: string;
  parentId?: number;
  parentName?: string;
  hasChildren?: boolean;
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
 * Fetch all categories from backend
 * Uses public endpoint - no authentication required
 */
export async function getCategories(parentId?: number): Promise<CategoryResponse[]> {
  try {
    const url = new URL(`${API_BASE_URL}/categories`);
    if (parentId !== undefined) {
      url.searchParams.append('parentId', parentId.toString());
    }

    console.log('Fetching categories from:', url.toString());

    const response = await fetch(url.toString(), {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      cache: 'no-store',
    });

    console.log('Categories response status:', response.status);

    if (!response.ok) {
      if (response.status === 401 || response.status === 403) {
        console.warn('Not authenticated - returning empty categories');
        return [];
      }
      throw new Error(`Failed to fetch categories: ${response.statusText}`);
    }

    const result: ApiResponse<CategoryResponse[]> = await response.json();
    console.log('Categories fetched:', result.data.length);
    return result.data;
  } catch (error) {
    console.error('Error fetching categories:', error);
    return [];
  }
}

/**
 * Fetch all root categories (parentId = null)
 */
export async function getRootCategories(): Promise<CategoryResponse[]> {
  return getCategories();
}

/**
 * Fetch child categories by parent ID
 */
export async function getChildCategories(parentId: number): Promise<CategoryResponse[]> {
  return getCategories(parentId);
}
