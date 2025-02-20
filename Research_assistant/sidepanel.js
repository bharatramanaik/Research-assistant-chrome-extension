// summerizbtn
// notes
// savenotesbtn
// results
document.addEventListener('DOMContentLoaded', () => {
    chrome.storage.local.get(['researchNotes'], function(result) {
        if (result.researchNotes) {
            document.getElementById('notes').value = result.researchNotes
        }
    });

    document.getElementById('summerizbtn').addEventListener('click', summerizetext);
    document.getElementById('savenotesbtn').addEventListener('click', savenotes);

});

async function summerizetext() {
    try {
        const [tab] = await chrome.tabs.query({active: true, currentWindow: true});
        const [executionResult] = await chrome.scripting.executeScript({
            target: {tabId: tab.id},
            function: () => window.getSelection().toString()
        });
        const result = executionResult?.result
        if (!result) {
            showResult("Please select some text")
            return
        }

        const response = await fetch("http://localhost:8080/api/research/process", {
            method: 'POST',
            headers: { 'Content-Type': 'application/json'},
            body: JSON.stringify({ content: result, operation: "summarize"})
        })

        if (!response.ok) {
            throw new Error("API error")
        }
        const text = await response.text();
        showResult(text.replace(/\n/g, '<br>'));
    } catch (error) {
        showResult("error:"+ error);
    }
}

async function savenotes() {
    const notes = document.getElementById('notes').value
    chrome.storage.local.set({'researchNotes': notes}, function() {
        alert("notes saved")
    })
}

function showResult(content) {
    document.getElementById('results').innerHTML = `<div class="result-item"><div class="result-content">${content}</div></div>`
}