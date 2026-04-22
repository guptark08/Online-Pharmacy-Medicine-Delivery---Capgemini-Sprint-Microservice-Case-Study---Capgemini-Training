import { 
  createBrowserRouter, 
  createRoutesFromElements, 
  Route 
} from "react-router-dom";

import RootLayout from "@/shared/components/layouts/RootLayout";
import NotFoundPage from "@/shared/components/NotFoundPage";
import HomePage from "@/features/home/pages/HomePage";

export const router = createBrowserRouter(
  createRoutesFromElements(
    <Route 
      path="/"
      element={<RootLayout />} 
      errorElement={<NotFoundPage />} 
    >
      <Route index element={<HomePage />} />
      {/*route for the main page "/"*/}

      {/* if user any unknow path the this page to transfer */}
      <Route path="*" element={<NotFoundPage />} /> // and this is used for the user side error while typing the url
    </Route>
  )
);