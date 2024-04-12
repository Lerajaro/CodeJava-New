// script.js
function processFile() {
    const fileInput = document.getElementById('fileInput');
    const file = fileInput.files[0];

    if (file) {
        const reader = new FileReader();

        reader.onload = function (e) {
            const fileContent = e.target.result;
            
            // Send file content to the backend (you can use AJAX, Fetch API, etc.)
            fetch('http://localhost:4567/process', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ content: fileContent }),
            })
            .then(response => response.json())
            .then(data => {
                console.log('Backend response:', data);
            })
            .catch(error => console.error('Error processing file:', error));
        };

        reader.readAsText(file);
    } else {
        console.error('No file selected.');
    }
}

function analyzeFile() {
    const fileInput = document.getElementById('csvFileInput');
    const selectedFile = fileInput.files[0];

    if (!selectedFile) {
        alert("Please choose a CSV file.");
        return;
    }

    const formData = new FormData();
    formData.append('file', selectedFile);

    fetch('http://localhost:4567/process', {
        method: 'POST',
        body: formData,
    })
    .then(response => {
        if (response.ok) {
            return response.json();
        } else {
            throw new Error('File processing failed');
        }
    })
    .then(data => {
        // Handle the backend response
        console.log('Backend response:', data);
        alert('File received successfully!');
    })
    .catch(error => {
        console.error('Error processing file:', error);
        alert('File processing failed.');
    });
}

