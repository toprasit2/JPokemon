package org.jpokemon.overworld;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.jpokemon.action.ActionFactory;
import org.jpokemon.action.ActionSet;
import org.jpokemon.pokemon.Pokemon;
import org.zachtaylor.jnodalxml.XmlNode;
import org.zachtaylor.jnodalxml.XmlParser;
import org.zachtaylor.myna.Myna;

public class Map {
  public static String mappath;

  static {
    Myna.configure(Map.class, "org.jpokemon.server");
    (solidPlaceholder = new Entity()).setSolid(true);
  }

  private String name;
  private int width, height;
  private Entity[][] entities;
  private List<WildPokemon> wildPokemon = new ArrayList<WildPokemon>();
  // used internally to determine entity sizes
  private int tilewidth, tileheight;

  private static final Entity solidPlaceholder;

  public Map(String name) {
    this.name = name;

    reload();
  }

  public String getName() {
    return name;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public Entity getEntityAt(int x, int y) {
    return entities[x][y];
  }

  public Pokemon getWildPokemon() {
    int totalFlex = 0;

    for (WildPokemon p : wildPokemon) {
      totalFlex += p.getFlex();
    }

    totalFlex = (int) (totalFlex * Math.random());

    for (WildPokemon p : wildPokemon) {
      if (totalFlex < p.getFlex()) {
        return p.instantiate();
      }
      else {
        totalFlex -= p.getFlex();
      }
    }

    return null;
  }

  public void reload() {
    File file = new File(mappath, name + ".tmx");
    if (!file.exists()) { throw new RuntimeException("Map does not exist: " + name); }

    XmlNode data;
    try {
      data = XmlParser.parse(file).get(1);
    }
    catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }

    width = data.getIntAttribute("width");
    height = data.getIntAttribute("height");
    tilewidth = data.getIntAttribute("tilewidth");
    tileheight = data.getIntAttribute("tileheight");

    entities = new Entity[width][height];

    String objectType;
    for (XmlNode objectgroup : data.getChildren("objectgroup")) {
      for (XmlNode object : objectgroup.getAllChildren()) {
        objectType = object.getAttribute("type");

        if ("solid".equals(objectType)) {
          parseSolid(object);
        }
        else if ("interact".equals(objectType)) {
          parseInteract(object);
        }
      }
    }

    wildPokemon.clear();
    for (WildPokemon wp : WildPokemon.get(name)) {
      wildPokemon.add(wp);
    }
  }

  private void parseSolid(XmlNode object) {
    Location location = parseLocation(object);

    for (int w = 0; w < location.getWidth(); w++) {
      for (int h = 0; h < location.getHeight(); h++) {
        if (entities[location.getLeft() + w][location.getTop() + h] != null) {
          continue;
        }

        entities[location.getLeft() + w][location.getTop() + h] = solidPlaceholder;
      }
    }
  }

  private void parseInteract(XmlNode object) {
    String name = object.getAttribute("name");

    Entity entity = new Entity();
    entity.setName(name);
    entity.setSolid(true);

    List<Interaction> interactions = Interaction.get("global", name);
    HashMap<Integer, ActionSet> actionSets = new HashMap<Integer, ActionSet>();
    for (Interaction interaction : interactions) {
      if (actionSets.get(interaction.getActiongroup()) == null) {
        actionSets.put(interaction.getActiongroup(), new ActionSet());
      }

      actionSets.get(interaction.getActiongroup()).addAction(ActionFactory.get(interaction.getAction(), interaction.getDataref()));
    }
    for (Entry<Integer, ActionSet> mapEntry : actionSets.entrySet()) {
      entity.addActionSet("interact", mapEntry.getValue());
    }

    Location location = parseLocation(object);
    for (int w = 0; w < location.getWidth(); w++) {
      for (int h = 0; h < location.getHeight(); h++) {
        entities[location.getLeft() + w][location.getTop() + h] = entity;
      }
    }
  }

  private Location parseLocation(XmlNode node) {
    // round down
    int x = node.getIntAttribute("x") / tilewidth;
    int y = node.getIntAttribute("y") / tileheight;
    // round up edge as displayed if it's there
    int w = 1, h = 1;
    if (node.hasAttribute("width")) {
      w += ((node.getIntAttribute("x") - x * tilewidth) + node.getIntAttribute("width")) / tilewidth;
    }
    if (node.hasAttribute("height")) {
      h += ((node.getIntAttribute("y") - y * tileheight) + node.getIntAttribute("height")) / tileheight;
    }

    Location location = new Location();
    location.setBounds(x, w, y, h);
    location.setMap(name);

    return location;
  }
}