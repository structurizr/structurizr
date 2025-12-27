structurizr.io.EncryptionStrategy = class EncryptionStrategy {

    #encryptionProperties = undefined;

    constructor(properties) {
        if (properties === undefined) {
            throw "Missing encryption properties"
        }

        this.#encryptionProperties = properties;

        if (this.#encryptionProperties.iv === undefined) {
            this.#encryptionProperties.iv = this.#generateSecureRandomBytesAsHex();
        }

        if (this.#encryptionProperties.salt === undefined) {
            this.#encryptionProperties.salt = this.#generateSecureRandomBytesAsHex();
        }

        this.#encryptionProperties.location = "Client";
    }

    decrypt(encryptedWorkspace) {
        const key = this.#generateEncryptionKey();

        const cipherParams = CryptoJS.lib.CipherParams.create({
            ciphertext: CryptoJS.enc.Base64.parse(encryptedWorkspace.ciphertext)
        });

        const decrypted = CryptoJS.AES.decrypt(
            cipherParams,
            key,
            {
                iv: CryptoJS.enc.Hex.parse(this.#encryptionProperties.iv)
            });

        return JSON.parse(decrypted.toString(CryptoJS.enc.Utf8));
    }

    encrypt(workspace) {
        const key = this.#generateEncryptionKey();
        const encrypted = CryptoJS.AES.encrypt(
            JSON.stringify(workspace),
            key,
            {
                iv: CryptoJS.enc.Hex.parse(this.#encryptionProperties.iv)
            });

        const ciphertext = encrypted.ciphertext.toString(CryptoJS.enc.Base64);

        return {
            id: workspace.id,
            name: workspace.name,
            description: workspace.description,
            ciphertext: ciphertext,
            encryptionStrategy: {
                type: this.#encryptionProperties.type,
                keySize: this.#encryptionProperties.keySize,
                iterationCount: this.#encryptionProperties.iterationCount,
                salt: this.#encryptionProperties.salt,
                iv: this.#encryptionProperties.iv,
                location: this.#encryptionProperties.location
            }
        };
    }

    #generateEncryptionKey() {
        return CryptoJS.PBKDF2(
            this.#encryptionProperties.passphrase,
            CryptoJS.enc.Hex.parse(this.#encryptionProperties.salt),
            {
                keySize: (this.#encryptionProperties.keySize / 32),
                iterations: this.#encryptionProperties.iterationCount
            });
    }

    #generateSecureRandomBytesAsHex() {
        var array = new Uint8Array(16);
        window.crypto.getRandomValues(array);
        var result = '';
        array.forEach(function(number) {
            result = result + ('0' + number.toString(16)).slice(-2);
        });

        return result;
    }

}