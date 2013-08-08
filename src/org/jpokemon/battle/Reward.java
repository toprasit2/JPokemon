package org.jpokemon.battle;

import java.util.ArrayList;
import java.util.List;

import org.jpokemon.action.ActionSet;
import org.jpokemon.activity.PlayerManager;
import org.jpokemon.activity.ServiceException;
import org.jpokemon.battle.slot.Slot;
import org.jpokemon.pokemon.EffortValue;
import org.jpokemon.pokemon.Pokemon;
import org.jpokemon.server.Message;
import org.jpokemon.trainer.Player;
import org.jpokemon.trainer.PokemonTrainer;
import org.jpokemon.trainer.Trainer;
import org.jpokemon.trainer.WildTrainer;
import org.zachtaylor.myna.Myna;

public class Reward {
  public static double wildxprate = 1.0;

  public static double npcxprate = 1.3;

  public static double gymxprate = 1.6;

  public static double playerxprate = 0.0;

  public static double otxprate = 1.0;

  public static double nototxprate = 1.5;

  static {
    Myna.configure(Reward.class, "org.jpokemon.battle.reward");
  }

  public Reward(Slot s) {
    _pokemon = s.leader();
    _faintMessage = new Message("BATTLE", _pokemon.name() + " fainted!", Message.Level.MESSAGE);

    _xp = computeXp(s.trainer(), _pokemon);

    if (!(s.trainer() instanceof Player)) {
      _evs = _pokemon.effortValues();
    }

    if (s.party().awake() == 0) {
      _defeatMessage = new Message("BATTLE", " defeated " + s.trainer().getName(), Message.Level.MESSAGE);

      for (RewardAction actionBinding : RewardAction.get(s.trainer().id())) {
        _actions.addAction(actionBinding.getAction());
      }
    }
  }

  public void xp(int amount) {
    _xp = amount;
  }

  public int xp() {
    return _xp;
  }

  public void effortValues(List<EffortValue> list) {
    _evs = list;
  }

  public List<EffortValue> effortValues() {
    return _evs;
  }

  public void pokemon(Pokemon p) {
    _pokemon = p;
  }

  public Pokemon pokemon() {
    return _pokemon;
  }

  public void apply(Slot s) {
    applyXP(s);

    if (s.trainer() instanceof Player) {
      PlayerManager.pushMessage((Player) s.trainer(), _faintMessage);

      if (_defeatMessage != null) {
        applyDefeat((Player) s.trainer());
      }
    }
  }

  private void applyXP(Slot s) {
    List<Pokemon> hitList = s.removeRival(pokemon());

    int xpEach = Math.max(xp() / hitList.size(), 1);

    /* TODO s is the number of Pokemon that participated in the battle and have
     * not fainted. If any Pokemon in the party is holding an Exp. Share, s is
     * equal to 2, and for the rest of the Pokemon, s is equal to twice the
     * number of Pokemon that participated instead. If more than one Pokemon is
     * holding an Exp. Share, s is equal to twice the number of Pokemon holding
     * the Exp. Share for each Pokemon holding one. */

    for (Pokemon earner : hitList) {
      if (earner.hasOriginalTrainer()) {
        earner.xp((int) (earner.xp() + xpEach * otxprate));
      }
      else {
        earner.xp((int) (earner.xp() + xpEach * nototxprate));
      }

      earner.addEV(effortValues());

      if (s.trainer() instanceof Player) {
        Message xpMessage = new Message("BATTLE", earner.name() + " received " + xpEach + " experience!", Message.Level.MESSAGE);
        PlayerManager.pushMessage((Player) s.trainer(), xpMessage);
      }

      s.trainer().notify();
    }
  }

  private void applyDefeat(Player player) {
    PlayerManager.pushMessage(player, _defeatMessage);

    try {
      _actions.execute(player);
    } catch (ServiceException e) {
    }
  }

  /**
   * Calculates the xp received from defeating the specified pokemon, as owned
   * by the specified trainer
   * 
   * @param trainer The Pokemon's Trainer
   * @param pokemon The Pokemon that fainted
   * @return The experience generated by defeating this combination
   */
  private static int computeXp(PokemonTrainer trainer, Pokemon pokemon) {
    double xp = pokemon.xpYield();

    xp *= pokemon.level();
    xp /= 7;

    if (trainer instanceof WildTrainer) {
      xp *= wildxprate;
    }
    else if (trainer instanceof Trainer) {
      if (((Trainer) trainer).isGym()) {
        xp *= gymxprate;
      }
      else {
        xp *= npcxprate;
      }
    }
    else if (trainer instanceof Player) {
      xp *= playerxprate;
    }

    return (int) xp;
  }

  private int _xp;
  private Pokemon _pokemon;
  private Message _faintMessage, _defeatMessage;
  private ActionSet _actions = new ActionSet();
  private List<EffortValue> _evs = new ArrayList<EffortValue>();
}