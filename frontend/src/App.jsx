import { useEffect, useMemo, useState } from 'react';
import { Activity, AlertCircle, ArrowDownRight, ArrowUpRight, Boxes, CirclePlus, Cpu, Gauge, Pencil, Plus, Radar, ShieldCheck, Trash2, X } from 'lucide-react';
import { api } from './services/api';

const emptyField = { fieldName: '', fieldType: 'NUMBER', required: false, dropdownOptions: '' };

export default function App() {
  const [tab, setTab] = useState('overview');
  const [fields, setFields] = useState([]);
  const [machines, setMachines] = useState([]);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [editing, setEditing] = useState(null);
  const [formOpen, setFormOpen] = useState(false);
  const [values, setValues] = useState({});
  const [fieldForm, setFieldForm] = useState(emptyField);
  const [search, setSearch] = useState('');
  const [predictions, setPredictions] = useState({});
  const [details, setDetails] = useState(null);

  async function refresh() {
    setBusy(true); setError('');
    try { const [f, m] = await Promise.all([api.fields(), api.machines()]); setFields(f); setMachines(m); }
    catch (e) { setError(e.message); }
    finally { setBusy(false); }
  }
  useEffect(() => { refresh(); }, []);

  const filtered = useMemo(() => machines.filter(m => m.machineName.toLowerCase().includes(search.toLowerCase())), [machines, search]);
  const highRiskCount = Object.values(predictions).filter(p => p.risk === 'High').length;

  function openForm(machine = null) {
    setEditing(machine);
    setFormOpen(true);
    const next = {};
    fields.forEach(field => { next[field.fieldName] = machine?.values?.[field.fieldName] ?? ''; });
    setValues(next); setError('');
  }
  async function saveMachine(event) {
    event.preventDefault(); setError('');
    try {
      if (editing) await api.updateMachine(editing.id, values); else await api.createMachine(values);
      setEditing(null); setFormOpen(false); setNotice(editing ? 'Machine updated.' : 'Machine added.'); await refresh();
    } catch (e) { setError(e.message); }
  }
  async function deleteMachine(machine) {
    if (!window.confirm(`Delete ${machine.machineName}?`)) return;
    try { await api.deleteMachine(machine.id); setNotice('Machine removed.'); await refresh(); }
    catch (e) { setError(e.message); }
  }
  async function predict(machine) {
    setPredictions(current => ({ ...current, [machine.id]: { loading: true } }));
    try { const result = await api.predict(machine.id); setPredictions(current => ({ ...current, [machine.id]: result })); }
    catch (e) { setPredictions(current => ({ ...current, [machine.id]: { error: e.message } })); }
  }
  async function addField(event) {
    event.preventDefault(); setError('');
    const payload = { ...fieldForm, dropdownOptions: fieldForm.fieldType === 'DROPDOWN' ? fieldForm.dropdownOptions.split(',').map(x => x.trim()).filter(Boolean) : [] };
    try { await api.addField(payload); setFieldForm(emptyField); setNotice('Field added to your machine form.'); await refresh(); }
    catch (e) { setError(e.message); }
  }

  return <div className="app-shell">
    <aside className="sidebar">
      <div className="brand"><div className="brand-mark"><Radar size={21}/></div><div><strong>Fieldnote</strong><span>Machine intelligence</span></div></div>
      <div className="workspace"><span className="workspace-dot"/> OPERATIONS <span className="workspace-chevron">⌄</span></div>
      <p className="nav-label">WORKSPACE</p>
      <nav>
        <button className={tab === 'overview' ? 'nav-item active' : 'nav-item'} onClick={() => setTab('overview')}><Activity size={17}/> Overview</button>
        <button className={tab === 'machines' ? 'nav-item active' : 'nav-item'} onClick={() => setTab('machines')}><Cpu size={17}/> Machines <span className="nav-count">{machines.length}</span></button>
        <button className={tab === 'fields' ? 'nav-item active' : 'nav-item'} onClick={() => setTab('fields')}><Boxes size={17}/> Field setup</button>
      </nav>
      <div className="sidebar-bottom"><div className="profile-avatar">JD</div><div className="profile-copy"><strong>Jordan Davis</strong><span>Plant administrator</span></div><span className="more">···</span></div>
    </aside>
    <main className="main-content">
      <header className="topbar"><div className="breadcrumb">Operations <span>/</span> <b>{tab === 'overview' ? 'Overview' : tab === 'machines' ? 'Machines' : 'Field setup'}</b></div><div className="top-actions"><span className="live-dot"/> Local system <span className="top-divider"/><span className="date-label">MON, SEP 28</span></div></header>
      <div className="page">
        {error && <div className="alert error"><AlertCircle size={17}/>{error}<button onClick={() => setError('')}><X size={16}/></button></div>}
        {notice && <div className="alert success"><ShieldCheck size={17}/>{notice}<button onClick={() => setNotice('')}><X size={16}/></button></div>}
        {tab === 'fields' ? <FieldsPage fields={fields} fieldForm={fieldForm} setFieldForm={setFieldForm} addField={addField}/> : <>
          <div className="page-heading"><div><div className="eyebrow"><span/> MACHINE MONITORING</div><h1>{tab === 'overview' ? 'Good morning, Jordan' : 'Machine registry'}</h1><p>{tab === 'overview' ? 'Here’s what’s happening across your equipment today.' : 'Manage your registered equipment and monitor risk.'}</p></div><button className="button primary" onClick={() => openForm()}><Plus size={17}/> Add machine</button></div>
          <section className="stats-grid">
            <StatCard icon={<Cpu size={17}/>} label="Total machines" value={machines.length.toString().padStart(2,'0')} note="Registered in system" color="blue" trend="+2 this month"/>
            <StatCard icon={<Gauge size={17}/>} label="Fields configured" value={fields.length.toString().padStart(2,'0')} note="Dynamic properties" color="purple" trend="Flexible schema"/>
            <StatCard icon={<Activity size={17}/>} label="Predictions run" value={Object.keys(predictions).length.toString().padStart(2,'0')} note="This session" color="green" trend="Local ML model"/>
            <StatCard icon={<ShieldCheck size={17}/>} label="High risk" value={highRiskCount.toString().padStart(2,'0')} note="Predicted machines" color="orange" trend="Needs attention"/>
          </section>
          <section className="machine-panel">
            <div className="panel-heading"><div><h2>{tab === 'overview' ? 'Equipment overview' : 'All machines'} <span className="subtle-count">{machines.length}</span></h2><p>Latest readings and local risk predictions</p></div><div className="panel-tools"><div className="search-box"><span>⌕</span><input value={search} onChange={e => setSearch(e.target.value)} placeholder="Search machines..."/></div><button className="button outline" onClick={() => setTab('machines')}>View all <ArrowUpRight size={15}/></button></div></div>
            <div className="table-wrap"><table><thead><tr><th>MACHINE</th><th>TEMPERATURE</th><th>PRESSURE</th><th>VIBRATION</th><th>RISK LEVEL</th><th>LAST UPDATED</th><th/></tr></thead><tbody>
              {busy ? <tr><td colSpan="7" className="empty-state">Loading equipment…</td></tr> : filtered.length === 0 ? <tr><td colSpan="7" className="empty-state">No machines yet. Add a machine to get started.</td></tr> : filtered.map(machine => <MachineRow key={machine.id} machine={machine} prediction={predictions[machine.id]} onPredict={() => predict(machine)} onEdit={() => openForm(machine)} onDelete={() => deleteMachine(machine)} onDetails={() => setDetails(machine)}/>)}
            </tbody></table></div>
            <div className="table-footer"><span>Showing <b>{filtered.length}</b> of <b>{machines.length}</b> machines</span><span className="footer-health"><span className="live-dot"/> All systems operational</span></div>
          </section>
          <div className="bottom-grid"><section className="insight-card"><div className="insight-icon"><Activity size={18}/></div><div><strong>Local risk intelligence</strong><p>Risk scores are generated on this device using a trained decision tree model. Machine readings stay in your local database.</p></div><span className="local-tag">LOCAL ONLY</span></section><section className="quick-card"><div className="quick-icon"><CirclePlus size={19}/></div><div><strong>Make your form yours</strong><p>Add machine properties without changing the database schema.</p></div><button className="quick-link" onClick={() => setTab('fields')}>Configure fields <ArrowDownRight size={14}/></button></section></div>
        </>}
      </div>
      {formOpen && <MachineModal fields={fields} values={values} setValues={setValues} editing={editing} onClose={() => setFormOpen(false)} onSubmit={saveMachine}/>}
      {details && <MachineDetailsModal machine={details} onClose={() => setDetails(null)}/>}
    </main>
  </div>;
}

function StatCard({ icon, label, value, note, color, trend }) { return <div className="stat-card"><div className={`stat-icon ${color}`}>{icon}</div><span className="stat-label">{label}</span><strong className="stat-value">{value}</strong><div className="stat-bottom"><span>{note}</span><em>{trend}</em></div></div>; }

function MachineRow({ machine, prediction, onPredict, onEdit, onDelete, onDetails }) {
  const vibe = machine.values.Vibration || '—';
  const risk = prediction?.risk;
  return <tr><td><button className="machine-cell machine-details-link" onClick={onDetails}><div className="machine-icon"><Cpu size={16}/></div><div><strong>{machine.machineName}</strong><span>Asset #{String(machine.id).padStart(4,'0')}</span></div></button></td>
    <td><span className="reading">{machine.values.Temperature ?? '—'}<small> °C</small></span></td><td><span className="reading">{machine.values.Pressure ?? '—'}<small> kPa</small></span></td>
    <td><span className={`vibration ${String(vibe).toLowerCase()}`}><i/>{vibe}</span></td>
    <td>{risk ? <span className={`risk-pill ${risk.toLowerCase()}`}><i/>{risk}</span> : prediction?.error ? <span className="risk-error" title={prediction.error}>Unavailable</span> : <button className="predict-button" disabled={prediction?.loading} onClick={onPredict}>{prediction?.loading ? 'Scoring…' : 'Run prediction'}</button>}</td>
    <td><span className="updated">{new Date(machine.updatedAt).toLocaleDateString(undefined,{month:'short',day:'numeric'})}<small>{new Date(machine.updatedAt).toLocaleTimeString(undefined,{hour:'2-digit',minute:'2-digit'})}</small></span></td>
    <td><div className="row-actions"><button aria-label="Edit machine" onClick={onEdit}><Pencil size={15}/></button><button aria-label="Delete machine" onClick={onDelete}><Trash2 size={15}/></button></div></td></tr>;
}

function FieldsPage({ fields, fieldForm, setFieldForm, addField }) {
  return <><div className="page-heading"><div><div className="eyebrow"><span/> CONFIGURATION</div><h1>Field setup</h1><p>Define the information captured for every machine.</p></div></div>
    <div className="fields-layout"><section className="machine-panel fields-list"><div className="panel-heading"><div><h2>Configured fields <span className="subtle-count">{fields.length}</span></h2><p>These fields appear in every machine form</p></div></div><div className="field-table">{fields.map((field,index)=><div className="field-row" key={field.id}><div className="field-order">{String(index+1).padStart(2,'0')}</div><div className="field-info"><strong>{field.fieldName}</strong><span>{field.fieldType === 'DROPDOWN' ? field.dropdownOptions.join(' · ') : field.fieldType === 'NUMBER' ? 'Numeric value' : 'Text value'}</span></div><span className="type-label">{field.fieldType}</span><span className={field.required ? 'required-label' : 'optional-label'}>{field.required ? 'Required' : 'Optional'}</span></div>)}</div><div className="schema-note"><ShieldCheck size={17}/><span>Fields are stored as data. Adding a new field does not require a database column or code change.</span></div></section>
      <section className="add-field-card"><div className="form-card-icon"><Plus size={19}/></div><h2>Add a field</h2><p>Create a property that will be available on machine records.</p><form onSubmit={addField}><label>Field name<input required maxLength="80" value={fieldForm.fieldName} onChange={e=>setFieldForm({...fieldForm,fieldName:e.target.value})} placeholder="e.g. Humidity"/></label><label>Field type<select value={fieldForm.fieldType} onChange={e=>setFieldForm({...fieldForm,fieldType:e.target.value,dropdownOptions:''})}><option value="TEXT">Text</option><option value="NUMBER">Number</option><option value="DROPDOWN">Dropdown</option></select></label>{fieldForm.fieldType==='DROPDOWN'&&<label>Options <span className="label-help">comma separated</span><input required value={fieldForm.dropdownOptions} onChange={e=>setFieldForm({...fieldForm,dropdownOptions:e.target.value})} placeholder="Low, Medium, High"/></label>}<label className="check-label"><input type="checkbox" checked={fieldForm.required} onChange={e=>setFieldForm({...fieldForm,required:e.target.checked})}/> Required field</label><button className="button primary full-width" type="submit"><Plus size={16}/> Create field</button></form></section></div></>;
}

function MachineModal({ fields, values, setValues, editing, onClose, onSubmit }) {
  return <div className="modal-backdrop" onMouseDown={e=>e.target===e.currentTarget&&onClose()}><section className="modal"><div className="modal-heading"><div><div className="eyebrow"><span/> MACHINE RECORD</div><h2>{editing ? 'Edit machine' : 'Add a machine'}</h2><p>Enter the latest details for this equipment.</p></div><button className="close-button" onClick={onClose}><X size={18}/></button></div><form onSubmit={onSubmit} className="machine-form">{fields.map(field=><label key={field.id}>{field.fieldName}{field.required&&<b className="required-star"> *</b>}{field.fieldType==='DROPDOWN'?<select required={field.required} value={values[field.fieldName]??''} onChange={e=>setValues({...values,[field.fieldName]:e.target.value})}><option value="">Select {field.fieldName.toLowerCase()}</option>{field.dropdownOptions.map(option=><option key={option}>{option}</option>)}</select>:<input required={field.required} type={field.fieldType==='NUMBER'?'number':'text'} step={field.fieldType==='NUMBER'?'any':undefined} value={values[field.fieldName]??''} onChange={e=>setValues({...values,[field.fieldName]:e.target.value})} placeholder={`Enter ${field.fieldName.toLowerCase()}`}/>}</label>)}<div className="modal-actions"><button type="button" className="button outline" onClick={onClose}>Cancel</button><button className="button primary" type="submit">{editing?'Save changes':'Add machine'}</button></div></form></section></div>;
}

function MachineDetailsModal({ machine, onClose }) {
  return <div className="modal-backdrop" onMouseDown={e=>e.target===e.currentTarget&&onClose()}><section className="modal details-modal"><div className="modal-heading"><div><div className="eyebrow"><span/> MACHINE DETAILS</div><h2>{machine.machineName}</h2><p>Asset #{String(machine.id).padStart(4,'0')} · Updated {new Date(machine.updatedAt).toLocaleString()}</p></div><button className="close-button" onClick={onClose}><X size={18}/></button></div><div className="detail-values">{Object.entries(machine.values).map(([name,value])=><div className="detail-value" key={name}><span>{name}</span><strong>{value === '' || value == null ? '—' : value}</strong></div>)}</div><div className="modal-actions"><button className="button primary" onClick={onClose}>Done</button></div></section></div>;
}
