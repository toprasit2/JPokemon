package org.jpokemon.pokemon;

import java.util.ArrayList;
import java.util.List;

import org.jpokemon.pokemon.move.Move;
import org.jpokemon.pokemon.move.MoveBlock;
import org.jpokemon.pokemon.stat.Stat;
import org.jpokemon.pokemon.stat.StatBlock;
import org.jpokemon.pokemon.stat.StatType;
import org.zachtaylor.jnodalxml.XmlException;
import org.zachtaylor.jnodalxml.XmlNode;

public class Pokemon {
  public static final String XML_NODE_NAME = "pokemon";

  public Pokemon(int num) {
    _number = num;
    _moves = new MoveBlock(_number);
    _species = PokemonInfo.get(_number);
    _stats = new StatBlock(_species);
  }

  public Pokemon(int num, int lvl) {
    this(num);
    _level = lvl;
    _stats.level(lvl);
    _moves.randomize(lvl);
  }

  public int number() {
    return _number;
  }

  public String name() {
    if (_name == null) return species();

    return _name;
  }

  public void name(String s) {
    _name = s;
  }

  public String species() {
    return _species.getName();
  }

  public int level() {
    return _level;
  }

  public int evolutionLevel() {
    return _species.getEvolutionlevel();
  }

  public int catchRate() {
    return _species.getCatchrate();
  }

  public void level(int l) {
    _level = l;
    _stats.level(l);

    checkNewMoves();

    conditionEffects = new ArrayList<ConditionEffect>();
  }

  public void incLevel() {
    // Truncate to tell if the next one crosses the whole number
    int thisLevelIncrement = (int) (level() / StatBlock.bonuslevelrate);
    int nextLevelIncrement = (int) (level() + 1 / StatBlock.bonuslevelrate);

    // Loop in case the difference is greater than 1
    while (nextLevelIncrement > thisLevelIncrement) {
      _stats.points(_stats.points() + 1);
      nextLevelIncrement--;
    }

    level(level() + 1);
  }

  public Type type1() {
    return Type.valueOf(_species.getType1());
  }

  public Type type2() {
    return Type.valueOf(_species.getType2());
  }

  public String getTrainerName() {
    return _ot;
  }

  public void setTrainerName(String s) {
    if (_ot != null)
      _hasOriginalTrainer = false;
    else {
      _hasOriginalTrainer = true;
      _ot = s;
    }
  }

  public boolean hasOriginalTrainer() {
    return _hasOriginalTrainer;
  }

  public int xp() {
    return _xp;
  }

  /**
   * Adds the xp specified to the Pokemon. If the Pokemon has enough, level is increased
   * 
   * @param amount Amount of xp to add
   */
  public void xp(int amount) {
    int xpNeededAtLevel = xpNeededAtLevel();

    _xp += amount;

    while (_xp >= xpNeededAtLevel) {
      _xp -= xpNeededAtLevel;
      incLevel();
    }
  }

  /**
   * XP this Pokemon needs to level
   * 
   * @return The amount of XP needed to gain a level
   */
  public int xpNeeded() {
    return xpNeededAtLevel() - xp();
  }

  public int xpYield() {
    return _species.getXpyield();
  }

  public Stat getStat(StatType s) {
    return _stats.get(s);
  }

  public int availableStatPoints() {
    return _stats.points();
  }

  public void statPoints(StatType s, int amount) {
    _stats.usePoints(s, amount);
  }

  public int health() {
    int val = getStat(StatType.HEALTH).cur();
    return val;
  }

  public int maxHealth() {
    return getStat(StatType.HEALTH).max();
  }

  public int attack() {
    return getStat(StatType.ATTACK).cur();
  }

  public int specattack() {
    return getStat(StatType.SPECATTACK).cur();
  }

  public int defense() {
    return getStat(StatType.DEFENSE).cur();
  }

  public int specdefense() {
    return getStat(StatType.SPECDEFENSE).cur();
  }

  public int speed() {
    return getStat(StatType.SPEED).cur();
  }

  public List<EffortValue> effortValues() {
    return _species.getEffortValues();
  }

  public void addEV(List<EffortValue> evs) {
    _stats.addEV(evs);
  }

  public Move move(int index) {
    return _moves.get(index);
  }

  public void addMove(int number) {
    _moves.add(number);
  }

  public int moveCount() {
    return _moves.count();
  }

  public void removeAllMoves() {
    _moves.removeAll();
  }

  /**
   * Takes a specified amount of damage. If damage is greater than available health, the Pokemon is knocked out.
   * 
   * @param damage The amount of damage to be taken
   * @return the awake state of the Pokemon
   */
  public void takeDamage(int damage) {
    getStat(StatType.HEALTH).effect(-damage);
  }

  /**
   * Heals specified damage. If healed amount is greater than missing health, Pokemon is brought to full health.
   * 
   * @param heal The amount healed by
   */
  public void healDamage(int heal) {
    getStat(StatType.HEALTH).effect(heal);
  }

  public boolean awake() {
    return health() > 0;
  }

  public void addConditionEffect(ConditionEffect e) {
    _stats.addConditionEffect(e);

    if (conditionEffects.contains(e)) {
      conditionEffects.remove(e);
    }

    conditionEffects.add(e);
  }

  public boolean hasConditionEffect(ConditionEffect e) {
    return conditionEffects.contains(e);
  }

  public List<ConditionEffect> getConditionEffects() {
    return conditionEffects;
  }

  public boolean removeConditionEffect(ConditionEffect e) {
    _stats.removeConditionEffect(e);
    return conditionEffects.remove(e);
  }

  public double catchBonus() {
    double best = 1;

    for (ConditionEffect e : conditionEffects) {
      best *= e.catchBonus();
    }

    return Math.min(best, 2.0);
  }

  /**
   * Changes a Pokemon into another one. This can be regular evolution (Charmander to Charmeleon) or other complicated changes (fire stone changes Eevee into
   * Flareon).
   */
  public void evolve(int... num) {
    if (num.length != 0)
      _number = num[0]; // special value
    else
      _number++;

    _moves.setPokemonNumber(_number);

    _species = PokemonInfo.get(_number);
    _stats.rebase(_species);

    checkNewMoves();
  }

  public XmlNode toXml() {
    XmlNode node = new XmlNode(XML_NODE_NAME);

    if (_name != null) {
      node.setAttribute("name", _name);
    }

    node.setAttribute("number", _number);
    node.setAttribute("level", _level);
    node.setAttribute("xp", _xp);
    node.setAttribute("ot", _ot);
    node.setAttribute("has_original_trainer", _hasOriginalTrainer);

    XmlNode conditionNode = new XmlNode("condition");
    conditionNode.setValue(conditionEffects.toString());
    node.addChild(conditionNode);

    node.addChild(_stats.toXml());
    node.addChild(_moves.toXml());

    return node;
  }

  public void loadXml(XmlNode node) {
    if (!XML_NODE_NAME.equals(node.getName())) throw new XmlException("Cannot read node");

    _name = node.getAttribute("name");
    _number = node.getIntAttribute("number");
    _moves.setPokemonNumber(_number);
    _species = PokemonInfo.get(_number);
    _stats.rebase(_species);

    _level = node.getIntAttribute("level");
    _stats.level(_level);

    _xp = node.getIntAttribute("xp");

    _ot = node.getAttribute("ot");

    _hasOriginalTrainer = node.getBoolAttribute("has_original_trainer");

    _moves.loadXml(node.getChildren(MoveBlock.XML_NODE_NAME).get(0));
    _stats.loadXml(node.getChildren(StatBlock.XML_NODE_NAME).get(0));

    String conditionString = node.getChildren("condition").get(0).getValue();
    if (conditionString != null) {
      for (String ce : conditionString.replace('[', ' ').replace(']', ' ').trim().split(",")) {
        if (ce.isEmpty()) {
          continue;
        }

        addConditionEffect(ConditionEffect.valueOf(ce.trim()));
      }
    }
  }

  private void checkNewMoves() {
    if (!_moves.newMoves(level()).isEmpty())
    ; // TODO : notify of new moves
  }

  private int xpNeededAtLevel() {
    GrowthRate rate = GrowthRate.valueOf(_species.getGrowthrate());

    return rate.xp(level());
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Pokemon)) return false;

    Pokemon p = (Pokemon) o;

    if (number() != p.number()) return false;
    if (level() != p.level()) return false;
    if (xp() != p.xp()) return false;
    if (!name().equals(p.name())) return false;
    // Probably good enough...

    return true;
  }

  @Override
  public int hashCode() {
    return (name().hashCode() & 255) + _number + _level;
  }

  private MoveBlock _moves;
  private StatBlock _stats;
  private String _name, _ot;
  private PokemonInfo _species;
  private int _number, _level, _xp;
  private boolean _hasOriginalTrainer;
  private List<ConditionEffect> conditionEffects = new ArrayList<ConditionEffect>();
}