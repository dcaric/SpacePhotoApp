# SpacePhotoApp

A simple desktop application that fetches and displays the Astronomy Picture of the Day (APOD) from NASA's official API. This project serves as a practical example of how to consume external RESTful APIs in a .NET desktop application, focusing on data fetching, display, and basic UI interaction.

---

## Table of Contents

* [Features](#features)
* [Technologies Used](#technologies-used)
* [Getting Started](#getting-started)
    * [Prerequisites](#prerequisites)
    * [Obtaining a NASA API Key](#obtaining-a-nasa-api-key)
    * [Installation](#installation)
    * [Running the Application](#running-the-application)
* [Project Structure](#project-structure)
* [How it Works](#how-it-works)
* [How to Contribute](#how-to-contribute)
* [License](#license)
* [Contact](#contact)

---

## Features

* **Daily Astronomy Picture:** Fetches and displays the Astronomy Picture of the Day (APOD) from NASA's API.
* **Image & Description:** Shows the stunning astronomical image along with its title, date, and a brief explanation provided by NASA.
* **External API Integration:** Demonstrates how to make asynchronous HTTP requests to a public REST API.
* **User-Friendly Interface:** Provides a simple graphical user interface to view the content.

---

## Technologies Used

* **.NET 8 (or later):** The core framework for the application.
* **WPF (Windows Presentation Foundation):** For building the rich desktop user interface.
* **C#:** The primary programming language.
* **`System.Net.Http.HttpClient`:** For making HTTP requests to the NASA API.
* **`System.Text.Json` (or Newtonsoft.Json):** For deserializing the JSON responses from the API into .NET objects.

---

## Getting Started

Follow these steps to set up and run the SpacePhotoApp on your local machine.

### Prerequisites

* **Visual Studio 2022 (or later):** With the ".NET desktop development" workload installed.
* **.NET SDK 8.0 (or later):** Ensure you have the correct .NET SDK installed.

### Obtaining a NASA API Key

The NASA APOD API requires an API key for access. You can get a free key (even a demo one for testing) from the official NASA API website:

1.  Go to the [NASA API Portal](https://api.nasa.gov/).
2.  Fill out the simple registration form to request your API key.
3.  You will receive your unique API key via email.

**Important:** For security, never hardcode your API key directly into your source code. This project expects you to place it in a configuration file or similar secure location.

### Installation

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/dcaric/SpacePhotoApp.git](https://github.com/dcaric/SpacePhotoApp.git)
    cd SpacePhotoApp
    ```
2.  **Open in Visual Studio:**
    Open the `SpacePhotoApp.sln` file in Visual Studio.
3.  **Configure your NASA API Key:**
    * Locate the appropriate place in the project where the API key is expected. This is typically in `App.config`, `appsettings.json` (if using .NET Core config patterns in WPF), or a specific constant/settings file.
    * Replace `YOUR_NASA_API_KEY_HERE` with the actual API key you obtained from NASA.

    *(Developer Note: A recommended approach is to use `appsettings.json` for API keys and ensure it's not committed to source control by adding it to `.gitignore` if it contains sensitive keys, or to use environment variables for production deployments.)*

### Running the Application

1.  **Restore NuGet Packages:** Visual Studio should automatically restore the necessary NuGet packages. If not, right-click on the solution in Solution Explorer and select "Restore NuGet Packages."
2.  **Build the Solution:** Press `Ctrl+Shift+B` or go to `Build > Build Solution`.
3.  **Run the Application:** Press `F5` or click the "Start" button in Visual Studio.

The application should launch, fetch the current Astronomy Picture of the Day, and display it.

---

## Project Structure
---

## How it Works

The application operates as follows:

1.  **Initialization:** When the application starts, it initializes an HTTP client.
2.  **API Call:** It constructs a request to the NASA APOD API (e.g., `https://api.nasa.gov/planetary/apod?api_key=YOUR_API_KEY`).
3.  **Data Fetching:** It makes an asynchronous GET request to the NASA APOD API.
4.  **JSON Deserialization:** Upon receiving a successful response, the JSON data is deserialized into a C# model object (e.g., `ApodData`).
5.  **UI Update:** The application then binds the data from the `ApodData` object to the UI elements in `MainWindow.xaml` to display the image, title, and explanation.
6.  **Error Handling:** (Ideally) If the API call fails or returns an error, appropriate messages are displayed to the user.

```
## How to Contribute

Contributions are welcome! If you have suggestions for improvements, new features, or bug fixes, please feel free to:

1.  Fork the repository.
2.  Create a new branch (`git checkout -b feature/YourFeature`).
3.  Make your changes.
4.  Commit your changes (`git commit -m 'Add some feature'`).
5.  Push to the branch (`git push origin feature/YourFeature`).
6.  Open a Pull Request.

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```

## Contact

If you have any questions or feedback about this project, please feel free to reach out:

* **Your GitHub Profile:** [https://github.com/dcaric](https://github.com/dcaric)
* **Your Website:** [www.dcapps.net](http://www.dcapps.net)
