import { ApiPersonResponse } from "@/types/apiTypes/ApiPersonResponse";
import { ApiTreeResponse } from "@/types/apiTypes/ApiTreeResponse";
import { FamilyGraph } from "@/types/FamilyGraph";

export function fetchBasicTree(personId:string) : Promise<ApiTreeResponse>{
     return fetch(`http://localhost:8080/person/${personId}/direct_tree`)
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })     
}
export function fetchSearchPersonByName(name:string) : Promise<ApiPersonResponse>{
     return fetch(`http://localhost:8080/person/name/${name}`)
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })     
}