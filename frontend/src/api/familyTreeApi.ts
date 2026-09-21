import { ApiResponse } from "@/types/apiTypes/ApiResponse";
import { FamilyGraph } from "@/types/FamilyGraph";

export function fetchBasicTree() : Promise<ApiResponse>{
     return fetch('http://localhost:8080/person/Q937/direct_tree')
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })     
}