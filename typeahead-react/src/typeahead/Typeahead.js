// Typeahead.js
import '../index.css'
import Messages from 'Messages.js'
import React from 'react';


export default class Typeahead extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      suggestions: [],
      text: ''
    }
  }

  onTextChange = (e) => {
    let suggestions = [];
    const value = e.target.value;
    if (value.length > 0) {
      const url = encodeURI(Messages.typeahead_api_part_1_uri + value + Messages.typeahead_api_part_2_uri);
      fetch(url)
        .then(res => res.json())
        .then(
          (result) => {
            this.setState({
              suggestions: result,
              text: value
            });
          },

          (error) => {
            this.setState({
              isLoaded: true,
              error
            });
          }
        )
    }

    this.setState(() => ({
      suggestions,
      text: value
    }));
  }

  suggestionSelected = (value) => {
    this.setState(() => ({
      text: value,
      suggestions: []
    }))
  }

  renderSuggestions = () => {
    const { suggestions } = this.state;
    if (suggestions.length === 0) {
      return null;
    }
    return (
      <ul>
        {suggestions.map(suggestion => <li key={suggestion} onClick={(e) => this.suggestionSelected(suggestion)}>{suggestion}</li>)}
      </ul>
    )
  }

  render() {
    const { text } = this.state
    return (
      <div className="Typeahead">
        <input onChange={this.onTextChange} placeholder="" value={text} type="text" />
        {this.renderSuggestions()}
      </div>
    );
  }
}