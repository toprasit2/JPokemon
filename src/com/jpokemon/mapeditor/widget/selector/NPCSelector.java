package com.jpokemon.mapeditor.widget.selector;

import org.jpokemon.map.npc.NPC;

public class NPCSelector extends JPokemonSelector<NPC> {
  @Override
  protected void reloadItems() {
    removeAllItems();

    NPC npc;
    for (int i = 1; (npc = NPC.get(i)) != null; i++) {
      addElementToModel(npc);
    }
  }
}