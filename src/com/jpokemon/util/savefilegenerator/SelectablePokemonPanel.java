package com.jpokemon.util.savefilegenerator;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jpokemon.pokemon.PokemonInfo;

import com.jpokemon.util.ui.ImageLoader;

public class SelectablePokemonPanel extends JPanel {
  public SelectablePokemonPanel(PokemonSearchPanel psp, PokemonInfo pi) {
    parent = psp;
    pokemonInfo = pi;

    setLayout(new BorderLayout());

    add(new JLabel(ImageLoader.pokemon(pi.getNumber() + "")), BorderLayout.WEST);

    add(new JLabel(pi.getName()), BorderLayout.CENTER);

    addButton.addMouseListener(new MouseHandler());
    add(addButton, BorderLayout.EAST);
  }

  private PokemonInfo pokemonInfo;
  private PokemonSearchPanel parent;
  private JLabel addButton = new JLabel(ImageLoader.find("ui/plus"));

  private static final long serialVersionUID = 1L;

  private class MouseHandler extends MouseAdapter {
    @Override
    public void mouseClicked(MouseEvent e) {
      parent.selectPokemon(pokemonInfo);
    }
  }
}