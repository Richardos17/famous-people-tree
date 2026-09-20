import { PersonConnectedEdges } from "@/app/types/PersonConnectedEdges";
import { XYPosition } from "@xyflow/react";

export function calculateParentPosition(rootNodePosition?:XYPosition, personEdgeNumber?:PersonConnectedEdges):XYPosition{
    if (rootNodePosition === undefined) {
      throw new Error("Position is undefined");
    }

    if (personEdgeNumber === undefined) {
      throw new Error("Root node is undefined");
    }
    
    let xPosition:number = 0;
    if(personEdgeNumber.top % 2 == 0)
      xPosition = rootNodePosition.x + 100 * (personEdgeNumber.top+1);
    else
      xPosition = rootNodePosition.x - 100 * (personEdgeNumber.top);
    return {x: xPosition , y: rootNodePosition.y -300};
  }
export function calculateChildPosition(rootNodePosition?:XYPosition, personEdgeNumber?:PersonConnectedEdges):XYPosition{
    if (rootNodePosition === undefined) {
      throw new Error("Position is undefined");
    }

    if (personEdgeNumber === undefined) {
      throw new Error("Root node is undefined");
    }
    
    let xPosition:number = 0;
    if(personEdgeNumber.top % 2 == 0)
      xPosition = rootNodePosition.x + 100 * (personEdgeNumber.top+1);
    else
      xPosition = rootNodePosition.x - 100 * (personEdgeNumber.top);
    return {x: xPosition , y: rootNodePosition.y + 300};
  }
export function calculateMarriagePosition(rootNodePosition?:XYPosition, personEdgeNumber?:PersonConnectedEdges):XYPosition{
    if (rootNodePosition === undefined) {
      throw new Error("Position is undefined");
    }

    if (personEdgeNumber === undefined) {
      throw new Error("Root node is undefined");
    }
    
    const xPosition:number = rootNodePosition.x + 500 * (personEdgeNumber.right+1);
    
    return {x: xPosition , y: rootNodePosition.y};
  }