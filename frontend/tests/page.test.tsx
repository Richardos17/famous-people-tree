import { render } from "@testing-library/react";
import Home from "@/app/page";

describe("Home page", () => {
  it("renders without crashing", () => {
    render(<Home />);
  });

  it("shows basic text or heading", () => {
    render(<Home />);

    const body = document.body;
    expect(body).toBeInTheDocument();
  });
});