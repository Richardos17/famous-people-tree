import { publicConfig } from "@/config/env";
import { ApiPersonResponse } from "@/types/apiTypes/ApiPersonResponse";
import { ApiTreeResponse } from "@/types/apiTypes/ApiTreeResponse";
import { FamilyGraph } from "@/types/FamilyGraph";

export function fetchBasicTree(personId:string, depth:number) : Promise<ApiTreeResponse>{
     return fetch(`${publicConfig.apiUrl}/person/${personId}/direct_tree?depth=${depth}`)
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })     
}
export function fetchSearchPersonByName(name:string) : Promise<ApiPersonResponse>{
     return fetch(`${publicConfig.apiUrl}/person/name/${name}`)
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })     
}