import axios from "axios";

const api = axios.create({
  baseURL: `${process.env.REACT_APP_API_BASE_URL}/client`, // Using environment variable
});

export const nodeUnregister = () => api.get("/unregister");
export const getRoutingTable = () => api.get("/routing-table");
export const searchFile = (filename) =>
  api.get(`/search-file?filename=${filename}`);
export const downloadFile = (filename) =>
  api.get(`/download-file?filename=${filename}`, { responseType: "blob" });
export const getCurrentNodeFiles = () => api.get("/files");
