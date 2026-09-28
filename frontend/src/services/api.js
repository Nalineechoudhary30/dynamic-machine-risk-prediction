const API = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

async function request(path, options = {}) {
  const response = await fetch(`${API}${path}`, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...options.headers },
  });
  if (!response.ok) {
    let message = `Request failed (${response.status})`;
    try { message = (await response.json()).error || message; } catch { /* use status message */ }
    throw new Error(message);
  }
  return response.status === 204 ? null : response.json();
}

export const api = {
  fields: () => request('/fields'),
  addField: (data) => request('/fields', { method: 'POST', body: JSON.stringify(data) }),
  machines: () => request('/machines'),
  machine: (id) => request(`/machines/${id}`),
  createMachine: (values) => request('/machines', { method: 'POST', body: JSON.stringify({ values }) }),
  updateMachine: (id, values) => request(`/machines/${id}`, { method: 'PUT', body: JSON.stringify({ values }) }),
  deleteMachine: (id) => request(`/machines/${id}`, { method: 'DELETE' }),
  predict: (id) => request(`/machines/${id}/predict`, { method: 'POST' }),
};
