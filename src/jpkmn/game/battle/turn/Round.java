package jpkmn.game.battle.turn;

import java.util.PriorityQueue;
import java.util.Queue;

import jpkmn.game.battle.Battle;
import jpkmn.game.battle.slot.Slot;
import jpkmn.game.pokemon.Condition;

public class Round {
  public Round(Battle b) {
    _battle = b;
    _turns = new PriorityQueue<Turn>();
  }

  public int size() {
    return _turns.size();
  }

  public void add(Turn t) {
    _turns.add(t);
  }

  public void execute() {
    // Setup rivals
    for (Slot a : _battle) {
      for (Slot b : _battle) {
        if (!a.trainer().equals(b))
          a.addRival(b);
      }
    }

    // MADNESS
    while (!_turns.isEmpty()) {
      Turn turn = _turns.remove();
      turn.execute();
      notifyAllTrainers(turn.getMessages());
      verifyTurnList();
    }

    applyEndOfRoundEffects();
  }

  private void verifyTurnList() {
    Slot slot;

    for (Turn turn : _turns) {
      slot = turn.slot();

      if (slot.party().awake() == 0) {
        _turns.remove(turn);
        _battle.remove(slot);
      }
      else if (!slot.leader().condition.awake()) {
        for (Slot s : _battle)
          s.removeRival(slot);

        turn.changeToSwap();
      }

      if (!_battle.contains(slot.target().trainer())) {
        // TODO shit bricks
      }
    }
  }

  private void applyEndOfRoundEffects() {
    // Condition effects
    for (Slot slot : _battle) {
      String[] messages = slot.leader().condition.applyEffects();
      notifyAllTrainers(messages);
    }

    // Slot effects
    for (Slot slot : _battle) {
      String[] messages = slot.applyEffects();
      notifyAllTrainers(messages);
    }

    // Force next attacks
    for (Slot slot : _battle)
      if (slot.leader().hasIssue(Condition.Issue.WAIT))
        _battle.add(slot.attack());
  }

  private void notifyAllTrainers(String[] message) {
    for (Slot s : _battle)
      s.trainer().notify(message);
  }

  private Battle _battle;
  private Queue<Turn> _turns;
}