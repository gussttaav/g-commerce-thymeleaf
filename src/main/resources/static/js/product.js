class ProductComponent {
    constructor() {
        document.addEventListener('DOMContentLoaded', () => {
            this.addProductStatusChangedListeners();
            this.addNewEditProductListeners();
        });
    }

    addProductStatusChangedListeners(){
        document.querySelectorAll(".status-toggle").forEach(checkbox => {
            checkbox.addEventListener("change", function () {
                const productId = this.getAttribute("data-product-id");
                const isActive = this.checked;
    
                fetch(`/productos/${productId}/status?activo=${isActive}`, {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json"
                    }
                })
                .then(response => {
                    if (!response.ok) {
                        throw new Error("Failed to update status");
                    }
                    UiUtils.showSuccess("Product status updated successfully!");
                })
                .catch(error => {
                    console.error(error);
                    UiUtils.showError("Error updating product status!");
                    this.checked = !isActive; // Revert checkbox if an error occurs
                });
            });
        });
    }

    addNewEditProductListeners(){
        const productModal = new bootstrap.Modal(document.getElementById("productModal"));
        const modalTitle = document.getElementById("productModalTitle");
        const productIdInput = document.getElementById("productId");
        const productNameInput = document.getElementById("productName");
        const productDescriptionInput = document.getElementById("productDescription");
        const productPriceInput = document.getElementById("productPrice");
        const saveProductButton = document.getElementById("saveProductButton");

        let isEditing = false;

        // Open modal for adding a new product
        document.getElementById("addProductButton").addEventListener("click", function () {
            isEditing = false;
            modalTitle.textContent = "Add Product";
            productIdInput.value = "";
            productNameInput.value = "";
            productDescriptionInput.value = "";
            productPriceInput.value = "";
            productModal.show();
        });

        // Open modal for editing a product
        document.querySelectorAll(".edit-product").forEach(button => {
            button.addEventListener("click", function () {
                isEditing = true;
                modalTitle.textContent = "Edit Product";

                const productId = this.getAttribute("data-product-id");

                // Fetch product details from backend
                fetch(`/productos/${productId}`)
                    .then(response => response.json())
                    .then(product => {
                        productIdInput.value = product.id;
                        productNameInput.value = product.nombre;
                        productDescriptionInput.value = product.descripcion || "";
                        productPriceInput.value = product.precio;
                        productModal.show();
                    })
                    .catch(error => console.error("Error fetching product:", error));
            });
        });

        // Handle form submission (Add/Edit)
        saveProductButton.addEventListener("click", function () {
            const productData = {
                nombre: productNameInput.value,
                descripcion: productDescriptionInput.value,
                precio: parseFloat(productPriceInput.value)
            };

            let url = "/productos/crear";
            let method = "POST";

            if (isEditing) {
                url = `/productos/actualizar/${productIdInput.value}`;
                method = "PUT";
            }

            fetch(url, {
                method: method,
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(productData)
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error("Failed to save product");
                }
                return response.json();
            })
            .then(() => {
                productModal.hide();
                location.reload(); // Refresh page to update product list
                UiUtils.showSuccess(
                    isEditing ? "Product information updated successfully." 
                              : "Product sucessfully saved!");
            })
            .catch(error => {
                console.error("Error saving product:", error);
                UiUtils.showError("Error saving the product!");
            });
        });
    }
}

const product = new ProductComponent();