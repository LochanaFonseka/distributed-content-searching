import React, { useState } from 'react';
import { searchFile, downloadFile } from '../api';

const SearchFile = () => {
  const [filename, setFilename] = useState('');
  const [results, setResults] = useState([]);

  const handleSearchFile = async () => {
    try {
      const response = await searchFile(filename);
      const resultsArray = Object.values(response.data);
      setResults(resultsArray);
    } catch (error) {
      console.error(error);
      alert('Failed to search file');
    }
  };

  const handleDownloadFile = async (filename) => {
    try {
      const response = await downloadFile(filename);
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
    } catch (error) {
      console.error(error);
      alert('Failed to download file');
    }
  };

  return (
    <div>
      <h2>Search File</h2>
      <input
        type="text"
        value={filename}
        onChange={(e) => setFilename(e.target.value)}
        placeholder="Enter file name"
      />
      <button onClick={handleSearchFile}>Search</button>
      {results.length > 0 && (
        <table className="search-table">
          <thead>
            <tr>
              <th>Option No</th>
              <th>File Name</th>
              <th>Source</th>
              <th>Query Hit Time (ms)</th>
              <th>Hop count</th>
              <th>Download</th>
            </tr>
          </thead>
          <tbody>
            {results.map((result, index) => (
              <tr key={index}>
                <td>{index + 1}</td>
                <td>{result.fileName}</td>
                <td>{`${result.address}:${result.port}`}</td>
                <td>{result.timeElapsed}</td>
                <td>{result.hops}</td>
                <td>
                  <button onClick={() => handleDownloadFile(`${result.address}:${result.port}${result.fileName}`)}>
                    Download
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default SearchFile;
