const $ = (selector) => document.querySelector(selector);

async function runQuery(useDemo = false) {
  const button = useDemo ? $("#demo") : $("#analyze");
  button.disabled = true;
  button.textContent = "Tracing evidence…";
  try {
    const response = useDemo
      ? await fetch("/api/demo")
      : await fetch("/api/query", {
          method: "POST",
          headers: {"Content-Type": "application/json"},
          body: JSON.stringify({
            question: $("#question").value,
            department: $("#department").value,
            jurisdiction: $("#jurisdiction").value
          })
        });
    if (!response.ok) throw new Error("The analysis request failed");
    render(await response.json());
  } catch (error) {
    $("#empty").innerHTML = `<h2>Analysis unavailable</h2><p>${escapeHtml(error.message)}</p>`;
  } finally {
    button.disabled = false;
    button.textContent = useDemo ? "Run governance demo →" : "Analyze against policy";
  }
}

function render(data) {
  $("#empty").hidden = true;
  $("#result").hidden = false;
  $("#confidence").textContent = data.grounded ? `${data.confidence}% GROUNDED` : "BLOCKED";
  $("#answer").textContent = data.answer;
  $("#evidence").innerHTML = data.evidence.map(item => `
    <article class="card">
      <small>${escapeHtml(item.documentTitle)} · ${escapeHtml(item.section)}</small>
      <span class="score">${Math.round(item.score * 100)}%</span>
      <p>${escapeHtml(item.excerpt)}</p>
    </article>`).join("") || '<p>No evidence passed the similarity threshold.</p>';
  $("#risks").innerHTML = data.risks.map(item => `
    <article class="card risk-${item.severity.toLowerCase()}">
      <small>${escapeHtml(item.severity)}</small>
      <p><b>${escapeHtml(item.title)}</b><br>${escapeHtml(item.recommendation)}</p>
    </article>`).join("") || '<article class="card"><p>No explicit control trigger detected.</p></article>';
  $("#trace").innerHTML = data.trace.map(step => `
    <div class="trace-row"><b>${escapeHtml(step.agent)}</b><span>${escapeHtml(step.status)}</span><p>${escapeHtml(step.summary)}</p></div>`).join("");
  $("#result").scrollIntoView({behavior: "smooth", block: "start"});
}

function escapeHtml(value) {
  const div = document.createElement("div");
  div.textContent = value ?? "";
  return div.innerHTML;
}

async function loadDocuments() {
  const response = await fetch("/api/documents");
  if (response.ok) $("#docCount").textContent = (await response.json()).length;
}

$("#demo").addEventListener("click", () => runQuery(true));
$("#analyze").addEventListener("click", () => runQuery(false));
loadDocuments();
