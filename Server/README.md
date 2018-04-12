# Server ReadMe File

### Software needed
* NodeJS (includes NPM Version > 6)
* MongoDB (if unable to connect to cloud server)


### Steps 
1. Install the software stated in the [Software needed](https://github.com/tiagoCMatias/AcmeCafe/tree/master/Server#software-needed) section.
2. Clone the **AcmeCafe** project using the command `https://github.com/tiagoCMatias/AcmeCafe.git` or download it manually [here](https://github.com/tiagoCMatias/AcmeCafe/archive/master.zip).
3. Go to the Server folder of the project
4. Inside the server folder run `npm install`
5. On the same location, run `npm start`
7. The sistem is now up and running. If the default ports are used, go to [http://localhost:3000](http://localhost:3000) to use AcmeCafe API

### Basic routes

* product
  * Get
    * /all - list all product
  * Post
    * /new - add new product (req. body `{"name": "product_name", "price": "product_price"}`)
* voucher
  * Get
    * /{:id} - list all vouchers of ID
* order
  * Post
    * /new - add new order (req. body `{"username": "username_id", "price": "total_price", "products:" "array_of_products", "vouchers": "array_of_vouchers"}`)
* User
  * Get
    * /{:id} - Checks if a given ID is associated with any user
  * Post
    * /new - add new user (req. body `{"name": "user_name", "nif": "user_nif", "public_key": "user_publicKey"}`)
