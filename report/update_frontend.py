import re

with open('tex_files/incident_flow.tex', 'r') as f:
    content = f.read()

old_str = r"""\subsection{Frontend Architecture and UI}
\label{subsec:impl_frontend}

The IncidentFlow frontend serves as a visual layer abstracting away the CLI restrictions, increasing accessibility for varying roles within security teams. It mirrors the CLI's utility in an intuitive, browser-based environment, relying entirely on the HTTP API backend for validation and compilation tasks.


The application provides a dual-model interaction interface: users can explicitly define scripts via the "Manual Editor" or auto-generate code via the visual "Scenario Wizard" (Figure~\ref{fig:ui_wizard}). In the Manual Editor (Figure~\ref{fig:ui_main}), text input is pushed to the API in real time.

When a user elects to generate a report from a pre-authored example, the UI loads playbooks from the backend (Figure~\ref{fig:ui_example}). If syntax or semantic faults are typed directly into the editor, the frontend catches HTTP 422 mapping errors, highlighting the exact mismatched tokens and line numbers via a red banner at the bottom of the interface (Figure~\ref{fig:ui_validate}). Finally, upon successful parsing, the frontend displays the fully templated HTML rendering of the generated Markdown (Figure~\ref{fig:ui_report_gen}), completing the visual feedback loop."""

new_str = r"""\subsection{Frontend Architecture and UI}
\label{subsec:impl_frontend}

The IncidentFlow frontend serves as a visual layer abstracting away the CLI restrictions, increasing accessibility for varying roles within security teams. Built using React and Vite, the modern web application provides a responsive, component-driven interface that ensures fast rendering and modular maintainability. It mirrors the CLI's utility in an intuitive, browser-based environment, relying entirely on the HTTP API backend for validation and compilation tasks.

\begin{figure}[htbp]
    \centering
    \includegraphics[width=0.8\textwidth]{images/main_page.png}
    \caption{The main editor interface of the IncidentFlow UI, built with React and Vite.}
    \label{fig:ui_main}
\end{figure}

The application provides a dual-model interaction interface: users can explicitly define scripts via the "Manual Editor" or auto-generate code via the visual "Scenario Wizard" (refer to Figure~\ref{fig:ui_wizard} in the Appendix). In the Manual Editor (Figure~\ref{fig:ui_main}), text input is pushed to the API in real time.

When a user elects to generate a report from a pre-authored example, the UI loads playbooks from the backend (see Figure~\ref{fig:ui_example} in the Appendix). If syntax or semantic faults are typed directly into the editor, the frontend catches HTTP 422 mapping errors, highlighting the exact mismatched tokens and line numbers via a red banner at the bottom of the interface (Figure~\ref{fig:ui_validate}). Finally, upon successful parsing, the frontend displays the fully templated HTML rendering of the generated Markdown (Figure~\ref{fig:ui_report_gen}), completing the visual feedback loop."""

content = content.replace(old_str, new_str)

with open('tex_files/incident_flow.tex', 'w') as f:
    f.write(content)
