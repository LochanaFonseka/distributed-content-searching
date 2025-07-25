import React, { useState, useEffect } from 'react';
import { getCurrentNodeFiles } from '../api';

const NodeFiles = () => {
  const [files, setFiles] = useState([]);

  const fetchFiles = async () => {
    try {
      const response = await getCurrentNodeFiles();
      setFiles(response.data.fileNameList); // Adjusted to match the API response structure
    } catch (error) {
      console.error('Error fetching files:', error);
      alert('Failed to fetch files');
    }
  };

  useEffect(() => {
    fetchFiles();
  }, []);

  return (
    <div className="node-files">
      <h2>Files we have</h2>
      <p>File count: {files.length}</p>
      <div className="files-list">
        {files.map((file, index) => (
          <p key={index}>{file}</p>
        ))}
      </div>
    </div>
  );
};

export default NodeFiles;
