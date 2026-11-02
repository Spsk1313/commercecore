export type MerchantResponse = {
  id: number;
  name: string;
};

export async function getMerchantById(
  merchantId: number,
): Promise<MerchantResponse> {
  const response = await fetch(`/api/v1/merchants/${merchantId}`);

  if (!response.ok) {
    if (response.status === 404) {
      throw new Error(`Merchant not found with id: ${merchantId}`);
    }
    throw new Error('Something went wrong');
  }

  const data = await response.json();

  return data as MerchantResponse;
}
