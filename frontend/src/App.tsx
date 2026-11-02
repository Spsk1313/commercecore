import { useState, type ChangeEvent, type SubmitEvent } from "react";
import { getMerchantById, type MerchantResponse } from "./api/merchants";

function App() {
  const [merchantIdInput, setMerchantIdInput] = useState("");
  const [error, setError] = useState("");
  const [merchant, setMerchant] = useState<MerchantResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(false);

  async function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();

    const input = merchantIdInput.trim();

    if (!/^\d+$/.test(input)) {
      setError("Merchant ID must contain only digits");
      setMerchant(null);
      return;
    }

    const merchantId = Number(input);

    if (!Number.isSafeInteger(merchantId) || merchantId <= 0) {
      setError("Merchant ID must be a positive integer");
      setMerchant(null);
      return;
    }

    setError("");
    setMerchant(null);
    setLoading(true);

    try {
      const result = await getMerchantById(merchantId);

      setMerchant(result);
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError("Unable to retrieve merchant");
      }
    } finally {
      setLoading(false);
    }

  }

  return (
    <main>
      <h1>CommerceCore</h1>
      <p>CommerceCore is a multi-merchant e-commerce application.</p>

      <form onSubmit={handleSubmit}>
        <label htmlFor="merchant-id">Merchant ID</label>

        <input
          id="merchant-id"
          type="text"
          value={merchantIdInput}
          onChange={(event: ChangeEvent<HTMLInputElement>) => setMerchantIdInput(event.target.value)}
          placeholder="Enter merchant ID"
          disabled={loading}
        />

        <button type="submit" disabled={loading}>{loading ? "Searching..." : "Find merchant"}</button>
      </form>

      {error && <p role="alert">{error}</p>}

      {merchant && (
        <section>
          <h2>Merchant details</h2>
          <p>ID: {merchant.id}</p>
          <p>Name: {merchant.name}</p>
        </section>
      )}
    </main>
  )
}

export default App;
