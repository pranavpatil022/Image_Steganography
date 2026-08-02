document.addEventListener('DOMContentLoaded', () => {
    // Encryption Elements
    const encInput = document.getElementById('enc-image-input');
    const encLabel = document.getElementById('enc-file-name');
    const encPreview = document.getElementById('enc-preview');
    const secretMessage = document.getElementById('secret-message');
    const encryptBtn = document.getElementById('encrypt-btn');

    // Decryption Elements
    const decInput = document.getElementById('dec-image-input');
    const decLabel = document.getElementById('dec-file-name');
    const decPreview = document.getElementById('dec-preview');
    const decryptBtn = document.getElementById('decrypt-btn');
    const decryptedOutput = document.getElementById('decrypted-output');

    // File Upload Handlers
    encInput.addEventListener('change', (e) => handleFileSelect(e, encLabel, encPreview));
    decInput.addEventListener('change', (e) => handleFileSelect(e, decLabel, decPreview));

    function handleFileSelect(event, labelElement, previewElement) {
        const file = event.target.files[0];
        if (file) {
            labelElement.textContent = file.name;
            const reader = new FileReader();
            reader.onload = (e) => {
                previewElement.innerHTML = `<img src="${e.target.result}" alt="Preview">`;
            };
            reader.readAsDataURL(file);
        }
    }

    // Encrypt Action
    encryptBtn.addEventListener('click', async () => {
        const file = encInput.files[0];
        const message = secretMessage.value;

        if (!file) {
            alert('Please select an image first.');
            return;
        }
        if (!message) {
            alert('Please enter a secret message.');
            return;
        }

        const formData = new FormData();
        formData.append('image', file);
        formData.append('message', message);

        try {
            encryptBtn.textContent = 'Processing...';
            encryptBtn.disabled = true;

            const response = await fetch('/api/encrypt', {
                method: 'POST',
                body: formData
            });

            if (response.ok) {
                const blob = await response.blob();
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = "secret_image.png";
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);
                alert('Encryption successful! Image downloaded.');
            } else {
                alert('Encryption failed. Please check if the image is valid.');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('An error occurred during encryption.');
        } finally {
            encryptBtn.textContent = 'Encrypt & Download';
            encryptBtn.disabled = false;
        }
    });

    // Decrypt Action
    decryptBtn.addEventListener('click', async () => {
        const file = decInput.files[0];

        if (!file) {
            alert('Please select an image to decrypt.');
            return;
        }

        const formData = new FormData();
        formData.append('image', file);

        try {
            decryptBtn.textContent = 'Decrypting...';
            decryptBtn.disabled = true;

            const response = await fetch('/api/decrypt', {
                method: 'POST',
                body: formData
            });

            if (response.ok) {
                const text = await response.text();
                decryptedOutput.value = text;
            } else {
                alert('Decryption failed. Ensure this is a valid stego image.');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('An error occurred during decryption.');
        } finally {
            decryptBtn.textContent = 'Decrypt Message';
            decryptBtn.disabled = false;
        }
    });
});
