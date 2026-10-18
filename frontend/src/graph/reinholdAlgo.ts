import { FamilyGraph } from "@/types/FamilyGraph";
import { Position } from "@xyflow/react";

export type PositionNode = {
  x: number;
  y: number;
  mod: number;
  shift: number;
  wikidataId: string;
  finalx: number;
};
type SimpleEdge = { fromWikidataId: string; toWikidataId: string };

type PositionTree = { positionNodes: PositionNode[]; edges: SimpleEdge[] };
export default function createPositionTree(
  familyGraph: FamilyGraph,
): PositionTree {
  let adjList = preprocessEdges(familyGraph);
  firstPassPartOne(adjList, "grf4", 0);
  return { positionNodes: [], edges: [] };
}
function preprocessEdges(
  familyGraph: FamilyGraph,
): Map<string, PositionNode[]> {
  const edgeMap: Map<string, PositionNode[]> = new Map();
  familyGraph.relationships.forEach((relationship) => {
    if (!edgeMap.has(relationship.fromWikidataId))
      edgeMap.set(relationship.fromWikidataId, []);
    edgeMap.get(relationship.fromWikidataId)?.push({
      wikidataId: relationship.toWikidataId,
      x: null,
      y: null,
      mod: null,
      shift: null,
    });
  });
  return edgeMap;
}
const siblingDist: number = 1;
const verticalDist: number = 20;
const subTreeDist: number = 1;
type RecursionData = {
  childrenMidpoint?: number;
  rightmostNodeST?: Map<number, number>;
  leftmostNodeCurrentST?: Map<number, number>;
};
export function firstPassPartOne(
  adjList: Map<string, PositionNode[]>,
  startNode: string,
  level: number,
): RecursionData {
  const children = adjList.get(startNode) || [];
  let firstNodeX: number = 0;
  let xPos: number = firstNodeX;

  children.forEach((node, i) => {
    if (
      adjList.has(node.wikidataId) &&
      adjList.get(node.wikidataId)!.length > 0
    ) {
      const recursionData = firstPassPartOne(
        adjList,
        node.wikidataId,
        level + 1,
      );
      if (i == 0) {
        firstNodeX = recursionData.childrenMidpoint;
        xPos = firstNodeX;
      } else node.mod = xPos - recursionData.childrenMidpoint;
    }

    node.x = xPos;
    node.y = level * verticalDist;
    xPos += siblingDist;
  });
  return { childrenMidpoint: (xPos - siblingDist + firstNodeX) / 2 };
}
export function firstPassPartTwo(
  adjList: Map<string, PositionNode[]>,
  startNode: string,
  level: number,
): RecursionData {
  const children = adjList.get(startNode) || [];
  const rightmostNodeSTMaps: Map<number, number>[] = [];
  let leftmostNodeST: Map<number, number> = new Map();
  children.forEach((node, i) => {
    let leftmostNodeCurrentST: Map<number, number> = new Map();
    if (
      adjList.has(node.wikidataId) &&
      adjList.get(node.wikidataId)!.length > 0
    ) {

      let recursionData = firstPassPartTwo(adjList, node.wikidataId, level + 1);
      rightmostNodeSTMaps.push(recursionData.rightmostNodeST);
      leftmostNodeCurrentST = recursionData.leftmostNodeCurrentST;
      if (i == 0) {
        leftmostNodeCurrentST.set(level, node.finalx);
      }
      leftmostNodeCurrentST.forEach((value, key) => {
        if(!leftmostNodeST.has(key) || leftmostNodeST.get(key) > value){
          leftmostNodeST.set(key, value)
        }
      });

    } else {
      if (i == 0) {
        leftmostNodeST.set(level, node.finalx);//TODO problem in overriding subtress at the end, better if it just coppies the values
        leftmostNodeCurrentST.set(level, node.finalx);//TODO maybe useless to compare the parent node
      }
      rightmostNodeSTMaps.push(new Map([[level, node.finalx]]));
    }

    
    if (i == 0) return;
    leftmostNodeCurrentST.forEach((value, key, map)=>map.set(key, value + node.shift ))
    rightmostNodeSTMaps[rightmostNodeSTMaps.length-1].forEach((value, key, map)=>map.set(key, value + node.shift ))
    let shiftNeeded = 0;
    let leftSubtreeIndex = -1;
    for (let j = 0; j < i; j++) {
      const xDistances: number[] = [];
      leftmostNodeCurrentST.forEach((value, key) => {
        if (node.wikidataId == "n") 
          console.log("g");
        if (
          rightmostNodeSTMaps[j] !== undefined &&
          rightmostNodeSTMaps[j].has(key)
        ) {
          xDistances.push(value - rightmostNodeSTMaps[j].get(key));
        }
      });
      if (
        subTreeDist > Math.min(...xDistances) &&
        subTreeDist - Math.min(...xDistances) > shiftNeeded
      ) {
        shiftNeeded = subTreeDist - Math.min(...xDistances);
        leftSubtreeIndex = j;
      }
    }
    const shiftCurrent = relativeShift(shiftNeeded, leftSubtreeIndex, i);
    for (let j = 0; j < children?.length; j++) {
      children[j].shift += (shiftCurrent * j) / i;
      rightmostNodeSTMaps[j]?.forEach((value, key)=>rightmostNodeSTMaps[j].set(key, value + (shiftCurrent * j) / i))
    }
  });
  const rightmostNodeSTMap = new Map<number, number>();
  rightmostNodeSTMaps.forEach(map =>{
    map.forEach((value, key) => rightmostNodeSTMap.set(key, value))
  })
  return {
    rightmostNodeST: rightmostNodeSTMap,
    leftmostNodeCurrentST: leftmostNodeST,
  };
}
export function secondPass(
  adjList: Map<string, PositionNode[]>,
  startNode: string,
  modAcc: number,
  shiftAcc: number
) {
  const children = adjList.get(startNode) || [];

  children.forEach((node) => {
    node.finalx = node.x + node.shift + shiftAcc + modAcc
    secondPass(adjList, node.wikidataId, modAcc + node.mod, shiftAcc + node.shift)
  });
}
function relativeShift(
  shiftNeeded: number,
  indexOfLeftElement: number,
  indexOfRightElement: number,
): number {
  return shiftNeeded / (1 - indexOfLeftElement / indexOfRightElement);
}
